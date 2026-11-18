package com.example.tournament.service.impl;

import com.example.tournament.dto.response.BracketResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.RankingCriteria;
import com.example.tournament.entity.RegistrationStatus;
import com.example.tournament.entity.Standing;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamRegistration;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.EntityMapper;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.StandingRepository;
import com.example.tournament.repository.TeamRegistrationRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.service.RankingService;
import com.example.tournament.util.BracketGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Moteur de calcul du classement.
 */
@Service
public class RankingServiceImpl implements RankingService {

    private static final int FORM_LENGTH = 5;

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final StandingRepository standingRepository;
    private final TeamRegistrationRepository registrationRepository;
    private final EntityMapper entityMapper;

    public RankingServiceImpl(TournamentRepository tournamentRepository,
                              MatchRepository matchRepository,
                              StandingRepository standingRepository,
                              TeamRegistrationRepository registrationRepository,
                              EntityMapper entityMapper) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.standingRepository = standingRepository;
        this.registrationRepository = registrationRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional
    public List<Standing> recompute(Tournament tournament) {
        if (tournament.getType() == TournamentType.ELIMINATION_DIRECTE) {
            return List.of();
        }

        List<TeamRegistration> registrations = registrationRepository
                .findByTournamentIdAndStatusOrderBySeedNumberAsc(tournament.getId(), RegistrationStatus.INSCRITE);
        List<Match> matches = matchRepository.findRealMatches(tournament.getId());

        Map<Long, Team> teams = new LinkedHashMap<>();
        Map<Long, MutableStats> stats = new HashMap<>();
        Map<Long, List<Character>> forms = new HashMap<>();

        for (TeamRegistration registration : registrations) {
            teams.put(registration.getTeam().getId(), registration.getTeam());
            stats.put(registration.getTeam().getId(), new MutableStats());
            forms.put(registration.getTeam().getId(), new ArrayList<>());
        }

        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.TERMINE || !match.hasScore() || !match.isReady()) {
                continue;
            }
            Long homeId = match.getHomeTeam().getId();
            Long awayId = match.getAwayTeam().getId();
            MutableStats home = stats.get(homeId);
            MutableStats away = stats.get(awayId);
            if (home == null || away == null) {
                continue;
            }
            int homeScore = match.getHomeScore();
            int awayScore = match.getAwayScore();

            home.played++;
            away.played++;
            home.goalsFor += homeScore;
            home.goalsAgainst += awayScore;
            away.goalsFor += awayScore;
            away.goalsAgainst += homeScore;

            if (homeScore > awayScore) {
                home.won++;
                away.lost++;
                forms.get(homeId).add('V');
                forms.get(awayId).add('D');
            } else if (homeScore < awayScore) {
                away.won++;
                home.lost++;
                forms.get(awayId).add('V');
                forms.get(homeId).add('D');
            } else {
                home.drawn++;
                away.drawn++;
                forms.get(homeId).add('N');
                forms.get(awayId).add('N');
            }
        }

        int pointsForWin = tournament.getPointsForWin() == null ? 3 : tournament.getPointsForWin();
        int pointsForDraw = tournament.getPointsForDraw() == null ? 1 : tournament.getPointsForDraw();

        List<Standing> standings = new ArrayList<>();
        for (Map.Entry<Long, Team> entry : teams.entrySet()) {
            MutableStats stat = stats.get(entry.getKey());
            Standing standing = new Standing();
            standing.setTournament(tournament);
            standing.setTeam(entry.getValue());
            standing.setPlayed(stat.played);
            standing.setWon(stat.won);
            standing.setDrawn(stat.drawn);
            standing.setLost(stat.lost);
            standing.setGoalsFor(stat.goalsFor);
            standing.setGoalsAgainst(stat.goalsAgainst);
            standing.setGoalDifference(stat.goalsFor - stat.goalsAgainst);
            standing.setPoints(stat.won * pointsForWin + stat.drawn * pointsForDraw);
            standing.setForm(buildForm(forms.get(entry.getKey())));
            standings.add(standing);
        }

        sortStandings(standings, tournament, matches, pointsForWin, pointsForDraw);
        assignRanks(standings);

        // Recalcul « à blanc » : on remplace les lignes existantes.
        // Le flush est indispensable : sans lui, Hibernate peut ordonner les INSERT avant
        // les DELETE et heurter la contrainte d'unicité (tournoi, équipe).
        standingRepository.deleteAll(standingRepository.findByTournamentId(tournament.getId()));
        standingRepository.flush();
        return standingRepository.saveAll(standings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Standing> getStandings(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw ResourceNotFoundException.of("Tournoi", tournamentId);
        }
        return standingRepository.findByTournamentIdOrderByRankAsc(tournamentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Standing> getTop(Long tournamentId, int limit) {
        return getStandings(tournamentId).stream().limit(limit).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BracketResponse buildBracket(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Tournoi", tournamentId));
        List<Match> matches = matchRepository.findByTournamentIdOrderByRoundNumberAscBracketPositionAsc(tournamentId);
        List<TeamRegistration> registrations =
                registrationRepository.findByTournamentIdAndStatusOrderBySeedNumberAsc(tournamentId, RegistrationStatus.INSCRITE);

        Map<MatchPhase, List<Match>> byPhase = new LinkedHashMap<>();
        for (MatchPhase phase : MatchPhase.values()) {
            List<Match> phaseMatches = matches.stream()
                    .filter(m -> m.getPhase() == phase)
                    .sorted(java.util.Comparator.comparing(Match::getRoundNumber)
                            .thenComparing(m -> m.getBracketPosition() == null ? 0 : m.getBracketPosition()))
                    .toList();
            if (!phaseMatches.isEmpty()) {
                byPhase.put(phase, phaseMatches);
            }
        }

        List<BracketResponse.BracketRoundResponse> rounds = new ArrayList<>();
        int order = 0;
        for (Map.Entry<MatchPhase, List<Match>> entry : byPhase.entrySet()) {
            rounds.add(entityMapper.toBracketRound(entry.getKey(), entry.getKey().getLibelle(), order++,
                    entry.getValue()));
        }

        Match finalMatch = matches.stream()
                .filter(m -> m.getPhase() == MatchPhase.FINALE)
                .findFirst()
                .orElse(null);
        Long championId = null;
        String championName = null;
        if (finalMatch != null && finalMatch.getWinner() != null) {
            championId = finalMatch.getWinner().getId();
            championName = finalMatch.getWinner().getName();
        }

        int bracketSize = BracketGenerator.bracketSize(registrations.size());
        int preliminary = registrations.isEmpty() ? 0 : registrations.size() - bracketSize;

        return new BracketResponse(
                tournament.getId(),
                tournament.getName(),
                registrations.size(),
                bracketSize,
                preliminary,
                Math.max(bracketSize - preliminary, 0),
                championId,
                championName,
                rounds);
    }

    // ------------------------------------------------------------------ Tri

    private void sortStandings(List<Standing> standings, Tournament tournament, List<Match> matches,
                               int pointsForWin, int pointsForDraw) {
        List<RankingCriteria> criteria = tournament.getRankingCriteria() == null || tournament.getRankingCriteria().isEmpty()
                ? List.of(RankingCriteria.POINTS, RankingCriteria.GOAL_DIFFERENCE, RankingCriteria.GOALS_SCORED)
                : tournament.getRankingCriteria();

        boolean useHeadToHead = criteria.contains(RankingCriteria.HEAD_TO_HEAD);
        List<RankingCriteria> effective = criteria.stream()
                .filter(c -> c != RankingCriteria.HEAD_TO_HEAD)
                .toList();

        Comparator<Standing> base = null;
        for (RankingCriteria criterion : effective) {
            Comparator<Standing> step = comparatorFor(criterion);
            base = (base == null) ? step : base.thenComparing(step);
        }
        if (base == null) {
            base = Comparator.comparing(s -> s.getTeam().getName(), String.CASE_INSENSITIVE_ORDER);
        }
        Comparator<Standing> finalComparator = base
                .thenComparing(s -> s.getTeam().getName(), String.CASE_INSENSITIVE_ORDER);

        standings.sort(finalComparator);

        if (useHeadToHead) {
            // Le regroupement des équipes à égalité se fait sur les critères « stats » uniquement :
            // le nom ne doit pas intervenir, sinon aucun groupe ne peut dépasser une équipe.
            applyHeadToHead(standings, matches, pointsForWin, pointsForDraw, base);
        }
    }

    private void applyHeadToHead(List<Standing> standings, List<Match> matches, int pointsForWin,
                                 int pointsForDraw, Comparator<Standing> tieBreaker) {
        Map<Long, MutableStats> headToHead = new HashMap<>();
        for (Standing standing : standings) {
            headToHead.put(standing.getTeam().getId(), new MutableStats());
        }
        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.TERMINE || !match.hasScore() || !match.isReady()) {
                continue;
            }
            MutableStats home = headToHead.get(match.getHomeTeam().getId());
            MutableStats away = headToHead.get(match.getAwayTeam().getId());
            if (home == null || away == null) {
                continue;
            }
            home.goalsFor += match.getHomeScore();
            home.goalsAgainst += match.getAwayScore();
            away.goalsFor += match.getAwayScore();
            away.goalsAgainst += match.getHomeScore();
            if (match.getHomeScore() > match.getAwayScore()) {
                home.won++;
                away.lost++;
            } else if (match.getHomeScore() < match.getAwayScore()) {
                away.won++;
                home.lost++;
            } else {
                home.drawn++;
                away.drawn++;
            }
        }

        Map<Long, Integer> points = new HashMap<>();
        headToHead.forEach((teamId, stat) ->
                points.put(teamId, stat.won * pointsForWin + stat.drawn * pointsForDraw));

        Comparator<Standing> h2h = Comparator
                .<Standing, Integer>comparing(s -> points.getOrDefault(s.getTeam().getId(), 0)).reversed()
                .thenComparing(s -> {
                    MutableStats stat = headToHead.get(s.getTeam().getId());
                    return stat == null ? 0 : stat.goalsFor - stat.goalsAgainst;
                }, Comparator.reverseOrder())
                .thenComparing(s -> {
                    MutableStats stat = headToHead.get(s.getTeam().getId());
                    return stat == null ? 0 : stat.goalsFor;
                }, Comparator.reverseOrder());

        // On regroupe les équipes à égalité sur les critères antérieurs, puis on
        // réordonne chaque groupe selon la confrontation directe.
        int start = 0;
        while (start < standings.size()) {
            int end = start + 1;
            while (end < standings.size() && tieBreaker.compare(standings.get(start), standings.get(end)) == 0) {
                end++;
            }
            if (end - start > 1) {
                List<Standing> group = new ArrayList<>(standings.subList(start, end));
                group.sort(h2h.thenComparing(s -> s.getTeam().getName(), String.CASE_INSENSITIVE_ORDER));
                for (int i = 0; i < group.size(); i++) {
                    standings.set(start + i, group.get(i));
                }
            }
            start = end;
        }
    }

    private Comparator<Standing> comparatorFor(RankingCriteria criterion) {
        return switch (criterion) {
            case POINTS -> Comparator.comparing(Standing::getPoints, Comparator.reverseOrder());
            case GOAL_DIFFERENCE -> Comparator.comparing(Standing::getGoalDifference, Comparator.reverseOrder());
            case GOALS_SCORED -> Comparator.comparing(Standing::getGoalsFor, Comparator.reverseOrder());
            case GOALS_CONCEDED -> Comparator.comparing(Standing::getGoalsAgainst);
            case WINS -> Comparator.comparing(Standing::getWon, Comparator.reverseOrder());
            case HEAD_TO_HEAD -> Comparator.comparing(Standing::getPoints, Comparator.reverseOrder());
        };
    }

    /** Classement avec ex æquo (1, 1, 3…). */
    private void assignRanks(List<Standing> standings) {
        int rank = 0;
        Standing previous = null;
        for (int i = 0; i < standings.size(); i++) {
            Standing current = standings.get(i);
            if (previous == null || !sameStats(previous, current)) {
                rank = i + 1;
            }
            current.setRank(rank);
            previous = current;
        }
    }

    private boolean sameStats(Standing a, Standing b) {
        return a.getPlayed().equals(b.getPlayed())
                && a.getWon().equals(b.getWon())
                && a.getDrawn().equals(b.getDrawn())
                && a.getLost().equals(b.getLost())
                && a.getGoalsFor().equals(b.getGoalsFor())
                && a.getGoalsAgainst().equals(b.getGoalsAgainst())
                && a.getPoints().equals(b.getPoints());
    }

    private String buildForm(List<Character> results) {
        if (results.isEmpty()) {
            return null;
        }
        int from = Math.max(0, results.size() - FORM_LENGTH);
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < results.size(); i++) {
            builder.append(results.get(i));
        }
        return builder.toString();
    }

    /** Conteneurs de statistiques réutilisables. */
    private static final class MutableStats {
        private int played;
        private int won;
        private int drawn;
        private int lost;
        private int goalsFor;
        private int goalsAgainst;
    }
}
