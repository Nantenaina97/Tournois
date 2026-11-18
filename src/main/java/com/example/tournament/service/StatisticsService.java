package com.example.tournament.service;

import com.example.tournament.dto.response.DashboardResponse;
import com.example.tournament.dto.response.TournamentStatisticsResponse;

import java.util.Map;

/** Statistiques et indicateurs. */
public interface StatisticsService {

    TournamentStatisticsResponse tournamentStatistics(Long tournamentId);

    DashboardResponse dashboard();

    Map<String, Long> matchesByStatus(Long tournamentId);
}
