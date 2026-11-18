package com.example.tournament.dto.response;

import java.util.List;
import java.util.Map;

/** Indicateurs du tableau de bord. */
public record DashboardResponse(
        long totalTournaments,
        long activeTournaments,
        long finishedTournaments,
        long totalTeams,
        long activeTeams,
        long totalMatches,
        long finishedMatches,
        long matchesToday,
        long matchesThisWeek,
        long pendingResults,
        long totalGoals,
        long playersCount,
        Map<String, Long> tournamentsByStatus,
        Map<String, Long> teamsByCity,
        List<TournamentResponse> recentTournaments,
        List<MatchResponse> upcomingMatches,
        List<TournamentResponse> liveTournaments) {
}
