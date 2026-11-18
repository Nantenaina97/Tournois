package com.example.tournament.service.impl;

import com.example.tournament.dto.request.GenerateMatchesRequest;
import com.example.tournament.dto.request.MatchResultRequest;
import com.example.tournament.dto.response.MatchGenerationResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.RegistrationStatus;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamRegistration;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.BusinessException;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.EntityMapper;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TeamRegistrationRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.RankingService;
import com.example.tournament.util.BracketGenerator;
import com.example.tournament.util.DateTimeUtils;
import com.example.tournament.util.MatchSpecifications;
import com.example.tournament.util.Pair;
import com.example.tournament.util.RoundRobinScheduler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service des matchs : génération automatique des rencontres,
 * saisie des résultats, détection des conflits et propagation dans le tableau
 * d'élimination directe.
 */
@Service
public class MatchServiceImpl implements MatchService {

    private static final int MIN_TEAMS = 2;
    private static final int DEFAULT_FIELDS = 4;
    private static final int MAX_KNOCKOUT_TEAMS = 64;

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final TeamRegistrationRepository registrationRepository;
    private final RankingService rankingService;
    private final EntityMapper entityMapper;

    public MatchServiceImpl(TournamentRepository tournamentRepository,
                            MatchRepository matchRepository,
                            TeamRegistrationRepository registrationRepository,
                            RankingService rankingService,
                            EntityMapper entityMapper) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.registrationRepository = registrationRepository;
        this.rankingService = rankingService;
        this.entityMapper = entityMapper;
    }

    // ------------------------------------------------------------------ Lecture

    @Override
    @Transactional(readOnly = true)
    public Optional<Match> findById(Long id) {
        return matchRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Match> search(Long tournamentId, MatchStatus status, MatchPhase phase, Integer roundNumber,
                              LocalDate from, LocalDate to, Long teamId, Pageable pageable) {
        Specification<Match> specification = MatchSpecifications.all(
                MatchSpecifications.tournamentId(tournamentId),
                MatchSpecifications.hasStatus(status),
                MatchSpecifications.hasPhase(phase),
                MatchSpecifications.hasRound(roundNumber),
                MatchSpecifications.betweenDates(from, to),
                MatchSpecifications.involvesTeam(teamId));
        return matchRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Match> findByTournament(Long tournamentId) {
        return matchRepository.findByTournamentIdOrderByRoundNumberAscBracketPositionAsc(tournamentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Match> findByTournamentAndPhase(Long tournamentId, MatchPhase phase) {
        return matchRepository.findByTournamentIdAndPhase(tournamentId, phase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Match> upcoming(int limit) {
        return matchRepository.findUpcoming(LocalDate.now(), Pageable.ofSize(limit));
    }

    @Override
    @Transactional(readOnly = true)
    public long countFinished(Long tournamentId) {
        return matchRepository.countFinished(tournamentId);
    }

    // -------------------------------------------------------------- Génération

    @Override
    @Transactional
    public MatchGenerationResponse generateMatches(Long tournamentId, GenerateMatchesRequest request) {
        Tournament tournament = getTournament(tournamentId);
        GenerateMatchesRequest options = request != null ? request : emptyOptions();

        if (tournament.getStatus().isClosed()) {
            throw new InvalidOperationException("Le tournoi est " + tournament.getStatus().getLibelle().toLowerCase()
                    + " : impossible de générer des rencontres.");
        }
        if (tournament.getStatus() == TournamentStatus.EN_COURS) {
            throw new InvalidOperationException("Le tournoi est en cours : le calendrier ne peut plus être modifié.");
        }
        if (matchRepository.existsByTournamentId(tournamentId)) {
            throw new ConflictException("Les rencontres de ce tournoi ont déjà été générées. "
                    + "Les matchs ne doivent pas être générés deux fois.");
        }

        List<Team> teams = activeTeams(tournamentId);
        if (teams.size() < MIN_TEAMS) {
            throw new InvalidOperationException("Au moins " + MIN_TEAMS
                    + " équipes doivent être inscrites pour générer un calendrier (actuellement : " + teams.size() + ").");
        }
        if (tournament.getType() == TournamentType.ELIMINATION_DIRECTE && teams.size() > MAX_KNOCKOUT_TEAMS) {
            throw new InvalidOperationException("L'élimination directe est limitée à " + MAX_KNOCKOUT_TEAMS + " équipes.");
        }

        LocalDate startDate = resolveStartDate(tournament, options);
        int daysBetween = options.daysBetweenRounds() != null ? options.daysBetweenRounds()
                : (tournament.getDaysBetweenRounds() == null ? 7 : tournament.getDaysBetweenRounds());
        int numberOfFields = options.numberOfFields() != null && options.numberOfFields() > 0
                ? options.numberOfFields() : DEFAULT_FIELDS;
        LocalTime kickoff = DateTimeUtils.parseTime(tournament.getKickoffTime(), LocalTime.of(15, 0));

        List<String> warnings = new ArrayList<>();
        List<Match> generated;
        int excludedTeams;
        int rounds;

        if (tournament.getType() == TournamentType.ELIMINATION_DIRECTE) {
            BracketGenerator.Plan plan = BracketGenerator.build(teams);
            generated = buildKnockoutMatches(tournament, plan, startDate, daysBetween, kickoff, numberOfFields, warnings);
            excludedTeams = plan.byes();
            rounds = plan.rounds().size();
        } else {
            List<List<Pair<Integer, Integer>>> schedule = tournament.getType() == TournamentType.CHAMPIONNAT_ALLER_RETOUR
                    ? RoundRobinScheduler.generateDouble(teams.size())
                    : RoundRobinScheduler.generateSingle(teams.size());
            generated = buildRoundRobinMatches(tournament, teams, schedule, startDate, daysBetween, kickoff, numberOfFields);
            excludedTeams = 0;
            rounds = schedule.size();
        }

        if (generated.isEmpty()) {
            throw new InvalidOperationException("Aucune rencontre n'a pu être générée pour ce tournoi.");
        }

        List<Match> saved = matchRepository.saveAll(generated);

        // Contrôle final : aucune équipe ne doit jouer deux matchs simultanément.
        ensureNoScheduleConflict(saved);

        if (tournament.getStatus() != TournamentStatus.PROGRAMME) {
            tournament.setStatus(TournamentStatus.PROGRAMME);
        }
        if (tournament.getEndDate() == null || saved.isEmpty()) {
            LocalDate last = saved.stream().map(Match::getMatchDate)
                    .filter(java.util.Objects::nonNull).max(Comparator.naturalOrder()).orElse(startDate);
            tournament.setEndDate(last);
        }
        tournamentRepository.save(tournament);

        if (tournament.getType() == TournamentType.ELIMINATION_DIRECTE) {
            rankingService.recompute(tournament);
        }

        String format = tournament.getType().getLibelle();
        return new MatchGenerationResponse(
                tournament.getId(),
                tournament.getName(),
                format,
                teams.size(),
                saved.size(),
                rounds,
                excludedTeams,
                entityMapper.toMatchResponses(saved),
                warnings);
    }

    private List<Match> buildRoundRobinMatches(Tournament tournament, List<Team> teams,
                                               List<List<Pair<Integer, Integer>>> schedule,
                                               LocalDate startDate, int daysBetween, LocalTime kickoff,
                                               int numberOfFields) {
        List<Match> matches = new ArrayList<>();
        int duration = duration(tournament);
        for (int roundIndex = 0; roundIndex < schedule.size(); roundIndex++) {
            LocalDate roundDate = startDate.plusDays((long) roundIndex * daysBetween);
            List<Pair<Integer, Integer>> round = schedule.get(roundIndex);
            for (int index = 0; index < round.size(); index++) {
                Pair<Integer, Integer> pairing = round.get(index);
                Match match = newMatch(tournament, MatchPhase.CHAMPIONNAT, roundIndex + 1,
                        "Journée " + (roundIndex + 1), index);
                match.setHomeTeam(teams.get(pairing.first()));
                match.setAwayTeam(teams.get(pairing.second()));
                match.setMatchDate(roundDate);
                match.setMatchTime(DateTimeUtils.slotTime(kickoff, index, duration));
                match.setField("Terrain " + ((index % numberOfFields) + 1));
                matches.add(match);
            }
        }
        return matches;
    }

    private List<Match> buildKnockoutMatches(Tournament tournament, BracketGenerator.Plan plan,
                                             LocalDate startDate, int daysBetween, LocalTime kickoff,
                                             int numberOfFields, List<String> warnings) {
        List<Match> matches = new ArrayList<>();
        int duration = duration(tournament);
        for (int roundIndex = 0; roundIndex < plan.rounds().size(); roundIndex++) {
            BracketGenerator.RoundPlan roundPlan = plan.rounds().get(roundIndex);
            LocalDate roundDate = startDate.plusDays((long) roundIndex * daysBetween);
            for (BracketGenerator.PlannedMatch planned : roundPlan.matches()) {
                Match match = newMatch(tournament, planned.phase(), roundPlan.roundNumber(),
                        planned.label(), planned.position());
                match.setHomeTeam(planned.first().team());
                match.setHomeLabel(match.getHomeTeam() != null ? match.getHomeTeam().getName() : planned.first().label());
                match.setAwayTeam(planned.second().team());
                match.setAwayLabel(match.getAwayTeam() != null ? match.getAwayTeam().getName() : planned.second().label());
                match.setMatchDate(roundDate);
                match.setMatchTime(DateTimeUtils.slotTime(kickoff, planned.position() % numberOfFields, duration));
                match.setField("Terrain " + ((planned.position() % numberOfFields) + 1));
                matches.add(match);
            }
        }
        if (plan.preliminaryMatches() > 0) {
            warnings.add(plan.numberOfTeams() + " équipes ne sont pas une puissance de deux : "
                    + plan.preliminaryMatches() + " match(s) de tour préliminaire ont été ajoutés et "
                    + plan.byes() + " équipes bénéficient d'un exempt.");
        }
        if (plan.byes() > 0 && plan.preliminaryMatches() == 0) {
            warnings.add(plan.byes() + " équipes bénéficient d'un exempt.");
        }
        return matches;
    }

    private Match newMatch(Tournament tournament, MatchPhase phase, int roundNumber, String label, int position) {
        Match match = new Match();
        match.setTournament(tournament);
        match.setPhase(phase);
        match.setRoundNumber(roundNumber);
        match.setPhaseLabel(label);
        match.setBracketPosition(position);
        match.setStatus(MatchStatus.PLANIFIE);
        match.setField(tournament.getVenue());
        return match;
    }

    // ------------------------------------------------------- Résultats & statut

    @Override
    @Transactional
    public Match saveResult(Long matchId, MatchResultRequest request) {
        Match match = getRequired(matchId);
        Tournament tournament = match.getTournament();

        if (match.isBye()) {
            throw new BusinessException("Un match d'exempt ne peut pas recevoir de résultat.");
        }
        if (!match.isReady()) {
            throw new InvalidOperationException(
                    "Le match n'est pas encore jouable : toutes les équipes ne sont pas déterminées.");
        }
        if (tournament.getStatus().isClosed()) {
            throw new InvalidOperationException("Le tournoi est " + tournament.getStatus().getLibelle().toLowerCase()
                    + " : aucun résultat ne peut être enregistré.");
        }
        if (!tournament.getStatus().isMatchPlayable()) {
            throw new InvalidOperationException("Le tournoi doit être programmé ou en cours pour enregistrer un résultat.");
        }
        if (request.homeScore() == null || request.awayScore() == null) {
            throw new BusinessException("Les deux scores sont obligatoires.");
        }
        if (request.homeScore() < 0 || request.awayScore() < 0) {
            throw new BusinessException("Les scores ne peuvent pas être négatifs.");
        }

        if (request.status() != null && request.status() != MatchStatus.TERMINE) {
            throw new BusinessException("La saisie d'un score clot automatiquement la rencontre : "
                    + "le statut " + request.status() + " n'est pas accepte ici.");
        }

        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        boolean knockout = tournament.getType() == TournamentType.ELIMINATION_DIRECTE;
        Team winner = resolveWinner(match, request, knockout);
        // Contrôle de planning avant d'enregistrer.
        LocalDate date = request.matchDate() != null ? request.matchDate() : match.getMatchDate();
        LocalTime time = request.matchTime() != null ? request.matchTime() : match.getMatchTime();
        ensureNoScheduleConflictForTeams(tournament, List.of(home.getId(), away.getId()), date, time,
                match.getId());

        match.setMatchDate(date);
        match.setMatchTime(time);
        if (!DateTimeUtils.isBlank(request.field())) {
            match.setField(request.field());
        }
        match.setHomeScore(request.homeScore());
        match.setAwayScore(request.awayScore());
        match.setWinner(winner);
        match.setStatus(MatchStatus.TERMINE);
        Match saved = matchRepository.saveAndFlush(match);

        if (saved.getStatus() == MatchStatus.TERMINE) {
            if (tournament.getStatus() == TournamentStatus.PROGRAMME) {
                tournament.setStatus(TournamentStatus.EN_COURS);
                tournamentRepository.save(tournament);
            }
            if (knockout) {
                advanceKnockout(tournament, saved, winner);
            } else {
                // Le classement est recalculé dans la même transaction que la saisie.
                rankingService.recompute(tournament);
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public Match updateMatch(Long matchId, MatchResultRequest request) {
        Match match = getRequired(matchId);
        Tournament tournament = match.getTournament();
        if (tournament.getStatus().isClosed() || tournament.getStatus() == TournamentStatus.EN_COURS) {
            throw new InvalidOperationException("Le tournoi « " + tournament.getName()
                    + " » n'accepte plus de modification de calendrier.");
        }
        LocalDate date = request.matchDate() != null ? request.matchDate() : match.getMatchDate();
        LocalTime time = request.matchTime() != null ? request.matchTime() : match.getMatchTime();
        if (match.isReady()) {
            ensureNoScheduleConflictForTeams(tournament,
                    List.of(match.getHomeTeam().getId(), match.getAwayTeam().getId()), date, time, match.getId());
        }
        match.setMatchDate(date);
        match.setMatchTime(time);
        if (!DateTimeUtils.isBlank(request.field())) {
            match.setField(request.field());
        }
        if (request.status() != null) {
            match.setStatus(request.status());
        }
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public Match changeStatus(Long matchId, MatchStatus status) {
        Match match = getRequired(matchId);
        Tournament tournament = match.getTournament();
        if (tournament.getStatus().isClosed()) {
            throw new InvalidOperationException("Le tournoi est clôturé : le statut du match ne peut plus changer.");
        }
        if (status == MatchStatus.TERMINE && !match.isReady()) {
            throw new InvalidOperationException("Un résultat ne peut pas être enregistré tant que les équipes ne sont pas déterminées.");
        }
        match.setStatus(status);
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public void deleteMatch(Long matchId) {
        Match match = getRequired(matchId);
        if (match.getStatus() == MatchStatus.TERMINE) {
            throw new InvalidOperationException("Un match ayant un résultat ne peut pas être supprimé.");
        }
        Tournament tournament = match.getTournament();
        if (tournament.getStatus() == TournamentStatus.EN_COURS) {
            throw new InvalidOperationException("Le tournoi est en cours : la suppression d'un match est interdite.");
        }
        matchRepository.delete(match);
    }

    // ------------------------------------------------------ Tableau d'élimination

    /**
     * Propage le vainqueur au tour suivant, élimine le perdant et clôture le tournoi
     * si la finale a été jouée.
     */
    private void advanceKnockout(Tournament tournament, Match match, Team winner) {
        if (winner == null) {
            return;
        }
        Team loser = match.getHomeTeam().getId().equals(winner.getId())
                ? match.getAwayTeam() : match.getHomeTeam();

        // 1. Match du tour suivant (toujours pré-généré).
        Optional<Match> nextRound = matchRepository.findByTournamentIdAndRoundNumberAndBracketPosition(
                tournament.getId(), match.getRoundNumber() + 1,
                BracketGenerator.nextPosition(match.getBracketPosition() == null ? 0 : match.getBracketPosition()));

        boolean isSemiFinal = match.getPhase() == MatchPhase.DEMI_FINALE;
        boolean isFinal = match.getPhase() == MatchPhase.FINALE;

        if (nextRound.isPresent() && !isSemiFinal && !isFinal) {
            Match next = nextRound.get();
            boolean firstSlot = (match.getBracketPosition() % 2 == 0);
            if (firstSlot) {
                if (next.getHomeTeam() == null) {
                    next.setHomeTeam(winner);
                    next.setHomeLabel(winner.getName());
                }
            } else {
                if (next.getAwayTeam() == null) {
                    next.setAwayTeam(winner);
                    next.setAwayLabel(winner.getName());
                }
            }
            matchRepository.save(next);
        }

        if (isSemiFinal) {
            createThirdPlaceMatchIfPossible(tournament);
        }

        if (isFinal) {
            closeKnockout(tournament, winner);
            return;
        }

        if (loser != null) {
            registrationRepository.findByTournamentIdAndTeamId(tournament.getId(), loser.getId())
                    .ifPresent(registration -> {
                        registration.setStatus(RegistrationStatus.ELIMINEE);
                        registrationRepository.save(registration);
                    });
        }
    }

    private void createThirdPlaceMatchIfPossible(Tournament tournament) {
        List<Match> semiFinals = matchRepository.findByTournamentIdAndPhase(tournament.getId(), MatchPhase.DEMI_FINALE);
        boolean allFinished = semiFinals.size() == 2 && semiFinals.stream()
                .allMatch(m -> m.getStatus() == MatchStatus.TERMINE && m.getWinner() != null);
        if (!allFinished) {
            return;
        }
        if (matchRepository.findByTournamentIdAndPhase(tournament.getId(), MatchPhase.MATCH_3E_PLACE).stream()
                .findAny().isPresent()) {
            return;
        }
        Team firstLoser = semiFinals.get(0).getHomeTeam().getId().equals(semiFinals.get(0).getWinner().getId())
                ? semiFinals.get(0).getAwayTeam() : semiFinals.get(0).getHomeTeam();
        Team secondLoser = semiFinals.get(1).getHomeTeam().getId().equals(semiFinals.get(1).getWinner().getId())
                ? semiFinals.get(1).getAwayTeam() : semiFinals.get(1).getHomeTeam();

        Match third = new Match();
        third.setTournament(tournament);
        third.setPhase(MatchPhase.MATCH_3E_PLACE);
        third.setPhaseLabel(MatchPhase.MATCH_3E_PLACE.getLibelle());
        third.setRoundNumber(semiFinals.get(0).getRoundNumber() + 1);
        third.setBracketPosition(0);
        third.setStatus(MatchStatus.PLANIFIE);
        third.setHomeTeam(firstLoser);
        third.setHomeLabel(firstLoser.getName());
        third.setAwayTeam(secondLoser);
        third.setAwayLabel(secondLoser.getName());
        LocalDate finalDate = matchRepository.findByTournamentIdAndPhase(tournament.getId(), MatchPhase.FINALE)
                .stream().map(Match::getMatchDate).filter(java.util.Objects::nonNull)
                .findFirst().orElse(tournament.getStartDate());
        third.setMatchDate(finalDate);
        third.setMatchTime(LocalTime.of(17, 0));
        third.setField(tournament.getVenue());
        matchRepository.save(third);
    }

    private void closeKnockout(Tournament tournament, Team champion) {
        for (TeamRegistration registration : registrationRepository.findByTournamentId(tournament.getId())) {
            boolean isChampion = registration.getTeam().getId().equals(champion.getId());
            registration.setStatus(isChampion ? RegistrationStatus.CHAMPIONNE
                    : registration.getStatus() == RegistrationStatus.INSCRITE ? RegistrationStatus.ELIMINEE
                    : registration.getStatus());
            registrationRepository.save(registration);
        }
        tournament.setStatus(TournamentStatus.TERMINE);
        tournamentRepository.save(tournament);
    }

    // ---------------------------------------------------------------- Contrôles

    private Team resolveWinner(Match match, MatchResultRequest request, boolean knockout) {
        Long homeId = match.getHomeTeam().getId();
        Long awayId = match.getAwayTeam().getId();
        int homeScore = request.homeScore();
        int awayScore = request.awayScore();

        if (homeScore > awayScore) {
            return match.getHomeTeam();
        }
        if (awayScore > homeScore) {
            return match.getAwayTeam();
        }
        // Égalité
        if (request.winnerTeamId() != null) {
            if (!request.winnerTeamId().equals(homeId) && !request.winnerTeamId().equals(awayId)) {
                throw new BusinessException("Le vainqueur indiqué ne participe pas à ce match.");
            }
            return request.winnerTeamId().equals(homeId) ? match.getHomeTeam() : match.getAwayTeam();
        }
        if (knockout) {
            throw new BusinessException("Égalité : le vainqueur doit être désigné (tirs au but) "
                    + "via le champ winnerTeamId.");
        }
        return null;
    }

    /** Aucune équipe ne peut disputer deux rencontres qui se chevauchent. */
    private void ensureNoScheduleConflict(List<Match> matches) {
        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                Match a = matches.get(i);
                Match b = matches.get(j);
                if (!shareTeam(a, b)) {
                    continue;
                }
                if (a.getMatchDate() == null || !a.getMatchDate().equals(b.getMatchDate())) {
                    continue;
                }
                int duration = duration(a.getTournament());
                if (overlaps(a.getMatchTime(), b.getMatchTime(), duration)) {
                    throw new ConflictException("Conflit de calendrier : "
                            + describe(a) + " et " + describe(b) + " mettent en jeu une même équipe au même moment.");
                }
            }
        }
    }

    private void ensureNoScheduleConflictForTeams(Tournament tournament, List<Long> teamIds, LocalDate date,
                                                  LocalTime time, Long excludeMatchId) {
        if (date == null || time == null) {
            return;
        }
        int duration = duration(tournament);
        for (Match other : matchRepository.findOnDate(date)) {
            if (other.getId().equals(excludeMatchId) || other.getStatus() == MatchStatus.ANNULE) {
                continue;
            }
            if (!other.getTournament().getId().equals(tournament.getId())) {
                continue;
            }
            boolean shared = teamIds.stream().anyMatch(id ->
                    (other.getHomeTeam() != null && other.getHomeTeam().getId().equals(id))
                            || (other.getAwayTeam() != null && other.getAwayTeam().getId().equals(id)));
            if (!shared) {
                continue;
            }
            if (overlaps(time, other.getMatchTime(), duration)) {
                throw new ConflictException("Une équipe ne peut pas disputer deux rencontres simultanément : "
                        + describe(other) + " est déjà programmé le " + date + " à " + other.getMatchTime() + ".");
            }
        }
    }

    private boolean shareTeam(Match a, Match b) {
        Long aHome = a.getHomeTeam() != null ? a.getHomeTeam().getId() : null;
        Long aAway = a.getAwayTeam() != null ? a.getAwayTeam().getId() : null;
        Long bHome = b.getHomeTeam() != null ? b.getHomeTeam().getId() : null;
        Long bAway = b.getAwayTeam() != null ? b.getAwayTeam().getId() : null;
        return java.util.Objects.equals(aHome, bHome) || java.util.Objects.equals(aHome, bAway)
                || java.util.Objects.equals(aAway, bHome) || java.util.Objects.equals(aAway, bAway);
    }

    private boolean overlaps(LocalTime a, LocalTime b, int durationMinutes) {
        if (a == null || b == null) {
            return false;
        }
        LocalTime aEnd = a.plusMinutes(durationMinutes);
        LocalTime bEnd = b.plusMinutes(durationMinutes);
        return a.isBefore(bEnd) && b.isBefore(aEnd);
    }

    private String describe(Match match) {
        String home = match.getHomeTeam() != null ? match.getHomeTeam().getName() : match.getHomeLabel();
        String away = match.getAwayTeam() != null ? match.getAwayTeam().getName() : match.getAwayLabel();
        return home + " - " + away;
    }

    // ---------------------------------------------------------------- Utilitaires

    private List<Team> activeTeams(Long tournamentId) {
        List<TeamRegistration> registrations = registrationRepository
                .findByTournamentIdAndStatusOrderBySeedNumberAsc(tournamentId, RegistrationStatus.INSCRITE);
        return registrations.stream()
                .sorted(Comparator.comparing(r -> r.getSeedNumber() == null ? Integer.MAX_VALUE : r.getSeedNumber()))
                .map(TeamRegistration::getTeam)
                .toList();
    }

    private LocalDate resolveStartDate(Tournament tournament, GenerateMatchesRequest options) {
        LocalDate start = options.startDate() != null ? options.startDate() : tournament.getStartDate();
        if (start == null) {
            return LocalDate.now();
        }
        return start.isBefore(LocalDate.now()) ? LocalDate.now() : start;
    }

    private int duration(Tournament tournament) {
        Integer configured = tournament.getMatchDurationMinutes();
        return configured == null || configured <= 0 ? 90 : configured;
    }

    private GenerateMatchesRequest emptyOptions() {
        return new GenerateMatchesRequest(null, null, null, null);
    }

    private Tournament getTournament(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Tournoi", id));
    }

    private Match getRequired(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Match", id));
    }
}
