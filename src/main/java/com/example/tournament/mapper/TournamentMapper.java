package com.example.tournament.mapper;

import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.entity.Tournament;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TeamRegistrationRepository;
import org.springframework.stereotype.Component;

/** Construction du DTO tournoi (complété par les compteurs issus des repositories). */
@Component
public class TournamentMapper {

    private final TeamRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;

    public TournamentMapper(TeamRegistrationRepository registrationRepository,
                            MatchRepository matchRepository) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
    }

    public TournamentResponse toResponse(Tournament tournament) {
        return toResponse(tournament, true, true);
    }

    public TournamentResponse toResponse(Tournament tournament, boolean withCounters, boolean withCountersDeep) {
        long teams = withCounters
                ? registrationRepository.countByTournamentIdAndStatus(
                        tournament.getId(), com.example.tournament.entity.RegistrationStatus.INSCRITE)
                : 0L;
        long matches = withCountersDeep ? matchRepository.countByTournamentId(tournament.getId()) : 0L;
        long finished = withCountersDeep ? matchRepository.countFinished(tournament.getId()) : 0L;

        return new TournamentResponse(
                tournament.getId(),
                tournament.getName(),
                tournament.getSport(),
                tournament.getSport() != null ? tournament.getSport().getLibelle() : null,
                tournament.getDescription(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.getVenue(),
                tournament.getMaxTeams(),
                tournament.getType(),
                tournament.getType() != null ? tournament.getType().getLibelle() : null,
                tournament.getStatus(),
                tournament.getStatus() != null ? tournament.getStatus().getLibelle() : null,
                tournament.getPointsForWin(),
                tournament.getPointsForDraw(),
                tournament.getRankingCriteria(),
                tournament.getDaysBetweenRounds(),
                tournament.getKickoffTime(),
                tournament.getMatchDurationMinutes(),
                teams,
                matches,
                finished,
                tournament.getCreatedAt(),
                tournament.getUpdatedAt());
    }
}
