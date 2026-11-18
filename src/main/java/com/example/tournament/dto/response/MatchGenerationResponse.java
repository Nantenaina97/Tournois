package com.example.tournament.dto.response;

import java.util.List;

/** Résultat d'une génération de rencontres. */
public record MatchGenerationResponse(
        Long tournamentId,
        String tournamentName,
        String format,
        int numberOfTeams,
        int generatedMatches,
        int rounds,
        int excludedTeams,
        List<MatchResponse> matches,
        List<String> warnings) {
}
