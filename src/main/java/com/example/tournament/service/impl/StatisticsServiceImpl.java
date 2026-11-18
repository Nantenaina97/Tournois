package com.example.tournament.service.impl;

import com.example.tournament.dto.response.DashboardResponse;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.dto.response.TournamentStatisticsResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.Standing;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.EntityMapper;
import com.example.tournament.mapper.TournamentMapper;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.PlayerRepository;
import com.example.tournament.repository.StandingRepository;
import com.example.tournament.repository.TeamRegistrationRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Calcul des statistiques du tournoi et du tableau de bord. */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final StandingRepository standingRepository;
    private final TeamRegistrationRepository registrationRepository;
    private final PlayerRepository playerRepository;
    private final TournamentMapper tournamentMapper;
    private final EntityMapper entityMapper;

    public StatisticsServiceImpl(TournamentRepository tournamentRepository,
                                 TeamRepository teamRepository,
                                 MatchRepository matchRepository,
                                 StandingRepository standingRepository,
                                 TeamRegistrationRepository registrationRepository,
                                 PlayerRepository playerRepository,
                                 TournamentMapper tournamentMapper,
                                 EntityMapper entityMapper) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.standingRepository = standingRepository;
        this.registrationRepository = registrationRepository;
        this.playerRepository = playerRepository;
        this.tournamentMapper = tournamentMapper;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentStatisticsResponse tournamentStatistics(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Tournoi", tournamentId));

        List<Match> matches = matchRepository.findByTournamentIdOrderByRoundNumberAscBracketPositionAsc(tournamentId);
        long finished = matches.stream().filter(m -> m.getStatus() == MatchStatus.TERMINE).count();
        long total = matches.size();
        long goals = matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.TERMINE && m.hasScore())
                .mapToLong(m -> (long) m.getHomeScore() + m.getAwayScore())
                .sum();

        List<Standing> standings = standingRepository.findByTournamentIdOrderByRankAsc(tournamentId);
        Standing best = standings.stream()
                .max(Comparator.comparing(Standing::getPoints)
                        .thenComparing(Standing::getGoalDifference)
                        .thenComparing(Standing::getGoalsFor))
                .orElse(null);

        List<Match> recent = matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.TERMINE)
                .sorted(Comparator.comparing(Match::getMatchDate,
                                Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .toList();

        return new TournamentStatisticsResponse(
                tournament.getId(),
                tournament.getName(),
                registrationRepository.countByTournamentIdAndStatus(tournamentId,
                        com.example.tournament.entity.RegistrationStatus.INSCRITE),
                total,
                finished,
                total - finished,
                total == 0 ? 0d : Math.round((finished * 1000d) / total) / 10d,
                goals,
                finished == 0 ? 0d : Math.round((goals * 100d) / finished) / 100d,
                best != null ? best.getTeam().getId() : null,
                best != null ? best.getTeam().getName() : null,
                best != null ? best.getWon() : 0,
                matchesByStatus(tournamentId),
                entityMapper.toStandingResponses(standings.stream().limit(5).toList()),
                entityMapper.toMatchResponses(recent));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> matchesByStatus(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentIdOrderByRoundNumberAscBracketPositionAsc(tournamentId);
        Map<String, Long> counters = new LinkedHashMap<>();
        for (MatchStatus status : MatchStatus.values()) {
            counters.put(status.name(), 0L);
        }
        for (Match match : matches) {
            counters.merge(match.getStatus().name(), 1L, Long::sum);
        }
        return counters;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        LocalDate today = LocalDate.now();
        List<Tournament> allTournaments = tournamentRepository.findAll();
        List<TournamentResponse> recentTournaments = allTournaments.stream()
                .sorted(Comparator.comparing(Tournament::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(tournamentMapper::toResponse)
                .toList();

        List<TournamentResponse> liveTournaments = allTournaments.stream()
                .filter(t -> t.getStatus() == TournamentStatus.EN_COURS || t.getStatus() == TournamentStatus.PROGRAMME)
                .limit(5)
                .map(tournamentMapper::toResponse)
                .toList();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TournamentStatus status : TournamentStatus.values()) {
            byStatus.put(status.name(), tournamentRepository.countByStatus(status));
        }

        Map<String, Long> byCity = teamRepository.findAll().stream()
                .filter(t -> t.getCity() != null && !t.getCity().isBlank())
                .collect(Collectors.groupingBy(Team::getCity, LinkedHashMap::new, Collectors.counting()));

        List<Match> todayMatches = matchRepository.findOnDate(today);
        List<Match> upcoming = matchRepository.findUpcoming(today, org.springframework.data.domain.Pageable.ofSize(8));

        long totalMatches = matchRepository.count();
        long finishedMatches = allTournaments.stream()
                .mapToLong(t -> matchRepository.countFinished(t.getId()))
                .sum();
        long totalGoals = allTournaments.stream()
                .flatMap(t -> matchRepository.findRealMatches(t.getId()).stream())
                .filter(m -> m.getStatus() == MatchStatus.TERMINE && m.hasScore())
                .mapToLong(m -> (long) m.getHomeScore() + m.getAwayScore())
                .sum();
        long pendingResults = todayMatches.stream()
                .filter(m -> m.getStatus() == MatchStatus.PLANIFIE || m.getStatus() == MatchStatus.EN_COURS)
                .count();

        return new DashboardResponse(
                tournamentRepository.count(),
                tournamentRepository.countByStatus(TournamentStatus.EN_COURS),
                tournamentRepository.countByStatus(TournamentStatus.TERMINE),
                teamRepository.count(),
                teamRepository.countByStatus(TeamStatus.ACTIF),
                totalMatches,
                finishedMatches,
                todayMatches.size(),
                matchRepository.findOnDate(today).stream()
                        .filter(m -> m.getTournament() != null)
                        .count(),
                pendingResults,
                totalGoals,
                playerRepository.count(),
                byStatus,
                byCity,
                recentTournaments,
                entityMapper.toMatchResponses(upcoming),
                liveTournaments);
    }
}
