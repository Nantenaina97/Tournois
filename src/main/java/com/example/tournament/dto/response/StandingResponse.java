package com.example.tournament.dto.response;

/** Ligne de classement. */
public record StandingResponse(
        Long tournamentId,
        Integer rank,
        Long teamId,
        String teamName,
        String teamCity,
        String teamLogoUrl,
        Integer played,
        Integer won,
        Integer drawn,
        Integer lost,
        Integer goalsFor,
        Integer goalsAgainst,
        Integer goalDifference,
        Integer points,
        String form) {
}
