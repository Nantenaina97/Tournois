package com.example.tournament.dto.response;

import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/** Représentation d'un match. */
public record MatchResponse(
        Long id,
        Long tournamentId,
        String tournamentName,
        Long homeTeamId,
        String homeTeamName,
        String homeTeamLogoUrl,
        String homeLabel,
        Long awayTeamId,
        String awayTeamName,
        String awayTeamLogoUrl,
        String awayLabel,
        LocalDate matchDate,
        LocalTime matchTime,
        String field,
        MatchPhase phase,
        String phaseLabel,
        String phaseDisplay,
        Integer roundNumber,
        Integer bracketPosition,
        MatchStatus status,
        String statusLabel,
        Integer homeScore,
        Integer awayScore,
        boolean bye,
        Long winnerTeamId,
        String winnerTeamName) {
}
