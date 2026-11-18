package com.example.tournament.controller.web;

import com.example.tournament.dto.response.DashboardResponse;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.StatisticsService;
import com.example.tournament.service.TournamentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Tableau de bord de l'application MVC. */
@Controller
public class DashboardController {

    private final StatisticsService statisticsService;
    private final TournamentService tournamentService;
    private final MatchService matchService;

    public DashboardController(StatisticsService statisticsService,
                               TournamentService tournamentService,
                               MatchService matchService) {
        this.statisticsService = statisticsService;
        this.tournamentService = tournamentService;
        this.matchService = matchService;
    }

    @GetMapping({"/", "/dashboard"})
    @PreAuthorize("hasAnyRole('USER','OPERATOR','MANAGER','ADMIN')")
    public String dashboard(Model model) {
        DashboardResponse dashboard = statisticsService.dashboard();
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("statusLabels", com.example.tournament.entity.TournamentStatus.values());
        model.addAttribute("upcoming", matchService.upcoming(10));
        return "dashboard";
    }
}
