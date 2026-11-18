package com.example.tournament.dto.response;

import com.example.tournament.entity.RankingCriteria;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Représentation complète d'un tournoi renvoyée par l'API. */
public record TournamentResponse(
        Long id,
        String name,
        Sport sport,
        String sportLabel,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String venue,
        Integer maxTeams,
        TournamentType type,
        String typeLabel,
        TournamentStatus status,
        String statusLabel,
        Integer pointsForWin,
        Integer pointsForDraw,
        List<RankingCriteria> rankingCriteria,
        Integer daysBetweenRounds,
        String kickoffTime,
        Integer matchDurationMinutes,
        Long registeredTeams,
        Long generatedMatches,
        Long finishedMatches,
        Instant createdAt,
        Instant updatedAt) {
}
