package com.example.tournament.dto.response;

import com.example.tournament.entity.MatchPhase;

import java.util.List;

/** Représentation du tableau d'élimination directe. */
public record BracketResponse(
        Long tournamentId,
        String tournamentName,
        Integer numberOfTeams,
        Integer bracketSize,
        int preliminaryMatches,
        int byes,
        Long championTeamId,
        String championTeamName,
        List<BracketRoundResponse> rounds) {

    /** Un tour du tableau. */
    public record BracketRoundResponse(
            MatchPhase phase,
            String label,
            int roundNumber,
            int order,
            List<MatchResponse> matches) {
    }
}
