package com.example.tournament.dto.response;

import java.time.LocalDate;

/** Historique de participation d'une équipe. */
public record TeamHistoryResponse(
        Long teamId,
        String teamName,
        Long tournamentId,
        String tournamentName,
        String sport,
        String tournamentType,
        String tournamentStatus,
        LocalDate startDate,
        LocalDate endDate,
        Integer position,
        Integer matchesPlayed,
        Integer wins,
        Integer draws,
        Integer losses,
        Integer goalsFor,
        Integer goalsAgainst,
        Integer points) {
}
