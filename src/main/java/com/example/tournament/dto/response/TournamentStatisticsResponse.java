package com.example.tournament.dto.response;

import java.util.List;
import java.util.Map;

/** Statistiques agrégées d'un tournoi. */
public record TournamentStatisticsResponse(
        Long tournamentId,
        String tournamentName,
        long totalTeams,
        long totalMatches,
        long finishedMatches,
        long remainingMatches,
        double completionRate,
        long totalGoals,
        double averageGoalsPerMatch,
        Long longestWinningStreakTeamId,
        String longestWinningStreakTeamName,
        int longestWinningStreak,
        Map<String, Long> matchesByStatus,
        List<StandingResponse> topTeams,
        List<MatchResponse> recentMatches) {
}
