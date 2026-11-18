package com.example.tournament.service.impl;

import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.response.TeamHistoryResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.Standing;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamRegistration;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.BusinessException;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.RequestMapper;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.StandingRepository;
import com.example.tournament.repository.TeamRegistrationRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.service.TeamService;
import com.example.tournament.util.TeamSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implémentation de la gestion des équipes. */
@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final StandingRepository standingRepository;
    private final RequestMapper requestMapper;

    public TeamServiceImpl(TeamRepository teamRepository,
                           TeamRegistrationRepository registrationRepository,
                           MatchRepository matchRepository,
                           StandingRepository standingRepository,
                           RequestMapper requestMapper) {
        this.teamRepository = teamRepository;
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.standingRepository = standingRepository;
        this.requestMapper = requestMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Team> search(String search, TeamStatus status, String city, Pageable pageable) {
        Specification<Team> specification = TeamSpecifications.all(
                TeamSpecifications.search(search),
                TeamSpecifications.hasStatus(status),
                TeamSpecifications.hasCity(city));
        return teamRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> findAll() {
        return teamRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return teamRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> findByIds(List<Long> ids) {
        return ids.isEmpty() ? List.of() : teamRepository.findAllById(ids);
    }

    @Override
    @Transactional
    public Team create(TeamRequest request) {
        if (teamRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Une équipe nommée « " + request.name().trim() + " » existe déjà.");
        }
        Team team = requestMapper.toEntity(request);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public Team update(Long id, TeamRequest request) {
        Team team = getRequired(id);
        if (!team.getName().equalsIgnoreCase(request.name().trim())
                && teamRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Une équipe nommée « " + request.name().trim() + " » existe déjà.");
        }
        requestMapper.applyTeam(request, team);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Team team = getRequired(id);
        List<TeamRegistration> registrations = registrationRepository.findHistoryOfTeam(id);
        boolean inProgress = registrations.stream()
                .map(TeamRegistration::getTournament)
                .anyMatch(t -> t.getStatus() == com.example.tournament.entity.TournamentStatus.EN_COURS
                        || t.getStatus() == com.example.tournament.entity.TournamentStatus.PROGRAMME
                        || t.getStatus() == com.example.tournament.entity.TournamentStatus.TERMINE);
        if (inProgress) {
            throw new InvalidOperationException(
                    "Impossible de supprimer cette équipe : elle est engagée dans un tournoi programmé, en cours ou terminé.");
        }
        List<Match> matches = matchRepository.findByTournamentIdOrderByRoundNumberAscBracketPositionAsc(
                        registrations.isEmpty() ? -1L : registrations.get(0).getTournament().getId());
        if (!matches.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer cette équipe : des matchs y sont rattachés.");
        }
        registrationRepository.deleteAll(registrations);
        team.getPlayers().clear();
        teamRepository.delete(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamHistoryResponse> getHistory(Long teamId) {
        Team team = getRequired(teamId);
        List<TeamRegistration> registrations = registrationRepository.findHistoryOfTeam(teamId);
        List<TeamHistoryResponse> history = new ArrayList<>();

        for (TeamRegistration registration : registrations) {
            Tournament tournament = registration.getTournament();
            List<Match> matches = matchRepository.findRealMatches(tournament.getId()).stream()
                    .filter(m -> involves(m, teamId))
                    .toList();

            int played = 0;
            int wins = 0;
            int draws = 0;
            int losses = 0;
            int goalsFor = 0;
            int goalsAgainst = 0;
            for (Match match : matches) {
                if (match.getStatus() != MatchStatus.TERMINE || !match.hasScore()) {
                    continue;
                }
                boolean home = match.getHomeTeam() != null && match.getHomeTeam().getId().equals(teamId);
                int scored = home ? match.getHomeScore() : match.getAwayScore();
                int conceded = home ? match.getAwayScore() : match.getHomeScore();
                played++;
                goalsFor += scored;
                goalsAgainst += conceded;
                if (scored > conceded) {
                    wins++;
                } else if (scored == conceded) {
                    draws++;
                } else {
                    losses++;
                }
            }

            Integer rank = null;
            Integer points = null;
            if (tournament.getType() != TournamentType.ELIMINATION_DIRECTE) {
                Optional<Standing> standing = standingRepository.findByTournamentIdAndTeamId(tournament.getId(), teamId);
                if (standing.isPresent()) {
                    rank = standing.get().getRank();
                    points = standing.get().getPoints();
                }
            } else if (tournament.getStatus() == com.example.tournament.entity.TournamentStatus.TERMINE) {
                rank = 1; // le champion est déterminé par le tableau
            }

            history.add(new TeamHistoryResponse(
                    team.getId(),
                    team.getName(),
                    tournament.getId(),
                    tournament.getName(),
                    tournament.getSport().getLibelle(),
                    tournament.getType().getLibelle(),
                    tournament.getStatus().getLibelle(),
                    tournament.getStartDate(),
                    tournament.getEndDate(),
                    rank,
                    played,
                    wins,
                    draws,
                    losses,
                    goalsFor,
                    goalsAgainst,
                    points));
        }
        return history;
    }

    private boolean involves(Match match, Long teamId) {
        return (match.getHomeTeam() != null && match.getHomeTeam().getId().equals(teamId))
                || (match.getAwayTeam() != null && match.getAwayTeam().getId().equals(teamId));
    }

    private Team getRequired(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Équipe", id));
    }
}
