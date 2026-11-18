package com.example.tournament.mapper;

import com.example.tournament.dto.response.BracketResponse;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.dto.response.PlayerResponse;
import com.example.tournament.dto.response.RegistrationResponse;
import com.example.tournament.dto.response.StandingResponse;
import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.Player;
import com.example.tournament.entity.RegistrationStatus;
import com.example.tournament.entity.Standing;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamRegistration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertisseur entité → DTO.
 *
 * <p>Les entités JPA ne sont jamais exposées par l'API : ce composant constitue
 * l'unique frontière de conversion.</p>
 */
@Component
public class EntityMapper {

    // ------------------------------------------------------------------ Players

    public PlayerResponse toPlayerResponse(Player player) {
        if (player == null) {
            return null;
        }
        return new PlayerResponse(
                player.getId(),
                player.getTeam() != null ? player.getTeam().getId() : null,
                player.getFirstName(),
                player.getLastName(),
                player.getFullName(),
                player.getPosition(),
                player.getJerseyNumber(),
                player.getNationality());
    }

    public List<PlayerResponse> toPlayerResponses(List<Player> players) {
        return players.stream().map(this::toPlayerResponse).toList();
    }

    // -------------------------------------------------------------------- Teams

    public TeamResponse toTeamResponse(Team team, boolean includePlayers) {
        if (team == null) {
            return null;
        }
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getLogoUrl(),
                team.getCity(),
                team.getCoach(),
                team.getCoachPhone(),
                team.getFoundedYear(),
                team.getStatus(),
                team.getStatus() != null ? team.getStatus().getLibelle() : null,
                (long) team.getPlayers().size(),
                (long) team.getRegistrations().size(),
                includePlayers ? toPlayerResponses(team.getPlayers()) : null);
    }

    // ------------------------------------------------------------ Registrations

    public RegistrationResponse toRegistrationResponse(TeamRegistration registration) {
        Team team = registration.getTeam();
        return new RegistrationResponse(
                registration.getId(),
                registration.getTournament().getId(),
                registration.getTournament().getName(),
                team.getId(),
                team.getName(),
                team.getCity(),
                team.getLogoUrl(),
                registration.getSeedNumber(),
                registration.getStatus(),
                registration.getStatus() != null ? registration.getStatus().getLibelle() : null,
                registration.getRegistrationDate());
    }

    // ------------------------------------------------------------------- Matchs

    public MatchResponse toMatchResponse(Match match) {
        if (match == null) {
            return null;
        }
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        return new MatchResponse(
                match.getId(),
                match.getTournament().getId(),
                match.getTournament().getName(),
                home != null ? home.getId() : null,
                home != null ? home.getName() : match.getHomeLabel(),
                home != null ? home.getLogoUrl() : null,
                match.getHomeLabel(),
                away != null ? away.getId() : null,
                away != null ? away.getName() : match.getAwayLabel(),
                away != null ? away.getLogoUrl() : null,
                match.getAwayLabel(),
                match.getMatchDate(),
                match.getMatchTime(),
                match.getField(),
                match.getPhase(),
                match.getPhase() != null ? match.getPhase().getLibelle() : null,
                buildPhaseLabel(match),
                match.getRoundNumber(),
                match.getBracketPosition(),
                match.getStatus(),
                match.getStatus() != null ? match.getStatus().getLibelle() : null,
                match.getHomeScore(),
                match.getAwayScore(),
                match.isBye(),
                match.getWinner() != null ? match.getWinner().getId() : null,
                match.getWinner() != null ? match.getWinner().getName() : null);
    }

    public List<MatchResponse> toMatchResponses(List<Match> matches) {
        return matches.stream().map(this::toMatchResponse).toList();
    }

    private String buildPhaseLabel(Match match) {
        if (match.getPhaseLabel() != null && !match.getPhaseLabel().isBlank()) {
            return match.getPhaseLabel();
        }
        MatchPhase phase = match.getPhase();
        if (phase == MatchPhase.CHAMPIONNAT) {
            return "Journée " + match.getRoundNumber();
        }
        return phase != null ? phase.getLibelle() : "";
    }

    // ---------------------------------------------------------------- Standings

    public StandingResponse toStandingResponse(Standing standing) {
        Team team = standing.getTeam();
        return new StandingResponse(
                standing.getTournament().getId(),
                standing.getRank(),
                team.getId(),
                team.getName(),
                team.getCity(),
                team.getLogoUrl(),
                standing.getPlayed(),
                standing.getWon(),
                standing.getDrawn(),
                standing.getLost(),
                standing.getGoalsFor(),
                standing.getGoalsAgainst(),
                standing.getGoalDifference(),
                standing.getPoints(),
                standing.getForm());
    }

    public List<StandingResponse> toStandingResponses(List<Standing> standings) {
        return standings.stream().map(this::toStandingResponse).toList();
    }

    // ------------------------------------------------------------------ Bracket

    public BracketResponse.BracketRoundResponse toBracketRound(MatchPhase phase, String label,
                                                                int order, List<Match> matches) {
        return new BracketResponse.BracketRoundResponse(
                phase, label, order, order, toMatchResponses(matches));
    }

    public String registrationStatusLabel(RegistrationStatus status) {
        return status != null ? status.getLibelle() : null;
    }
}
