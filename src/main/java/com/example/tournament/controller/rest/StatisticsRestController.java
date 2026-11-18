package com.example.tournament.controller.rest;

import com.example.tournament.dto.response.DashboardResponse;
import com.example.tournament.dto.response.TournamentStatisticsResponse;
import com.example.tournament.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** API REST des statistiques. */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsRestController {

    private final StatisticsService statisticsService;

    public StatisticsRestController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(statisticsService.dashboard());
    }

    @GetMapping("/tournaments/{id}")
    public ResponseEntity<TournamentStatisticsResponse> tournament(@PathVariable Long id) {
        return ResponseEntity.ok(statisticsService.tournamentStatistics(id));
    }

    @GetMapping("/tournaments/{id}/matches-by-status")
    public ResponseEntity<Map<String, Long>> matchesByStatus(@PathVariable Long id) {
        return ResponseEntity.ok(statisticsService.matchesByStatus(id));
    }
}
