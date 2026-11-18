package com.example.tournament.controller.web;

import com.example.tournament.dto.request.RegistrationForm;
import com.example.tournament.entity.RegistrationStatus;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamRegistration;
import com.example.tournament.entity.Tournament;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.RankingService;
import com.example.tournament.service.TeamService;
import com.example.tournament.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Détail d'un tournoi, gestion des équipes inscrites et génération du calendrier. */
@Controller
@RequestMapping("/tournaments/{id}")
public class TournamentDetailWebController {

    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final MatchService matchService;
    private final RankingService rankingService;
    private final com.example.tournament.service.StatisticsService statisticsService;

    public TournamentDetailWebController(TournamentService tournamentService,
                                         TeamService teamService,
                                         MatchService matchService,
                                         RankingService rankingService,
                                         com.example.tournament.service.StatisticsService statisticsService) {
        this.tournamentService = tournamentService;
        this.teamService = teamService;
        this.matchService = matchService;
        this.rankingService = rankingService;
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public String detail(@PathVariable Long id, Model model) {
        Tournament tournament = getTournament(id);
        List<TeamRegistration> registrations = tournamentService.getRegistrations(id);
        model.addAttribute("tournament", tournament);
        model.addAttribute("registrations", registrations);
        model.addAttribute("statuses", com.example.tournament.entity.TournamentStatus.values());
        model.addAttribute("matchCount", matchService.search(id, null, null, null, null, null, null,
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
        model.addAttribute("finishedCount", matchService.countFinished(id));
        return "tournament/detail";
    }

    @GetMapping("/teams")
    public String teams(@PathVariable Long id, Model model) {
        Tournament tournament = getTournament(id);
        List<TeamRegistration> registrations = tournamentService.getRegistrations(id);
        List<Long> registeredIds = registrations.stream().map(r -> r.getTeam().getId()).toList();
        List<Team> available = teamService.findAll().stream()
                .filter(team -> !registeredIds.contains(team.getId()))
                .toList();
        model.addAttribute("tournament", tournament);
        model.addAttribute("registrations", registrations);
        model.addAttribute("availableTeams", available);
        model.addAttribute("registrationForm", new RegistrationForm());
        model.addAttribute("slotsLeft", Math.max(tournament.getMaxTeams() - registrations.size(), 0));
        return "tournament/teams";
    }

    @PostMapping("/teams")
    public String registerTeam(@PathVariable Long id,
                               @ModelAttribute("registrationForm") RegistrationForm form,
                               RedirectAttributes redirectAttributes) {
        tournamentService.registerTeam(id, form.getTeamId(), form.getSeedNumber());
        redirectAttributes.addFlashAttribute("success", "L'équipe a été inscrite au tournoi.");
        return "redirect:/tournaments/" + id + "/teams";
    }

    @PostMapping("/teams/{teamId}/remove")
    public String unregisterTeam(@PathVariable Long id, @PathVariable Long teamId,
                                 RedirectAttributes redirectAttributes) {
        tournamentService.unregisterTeam(id, teamId);
        redirectAttributes.addFlashAttribute("success", "L'équipe a été retirée du tournoi.");
        return "redirect:/tournaments/" + id + "/teams";
    }

    @GetMapping("/matches")
    public String matches(@PathVariable Long id,
                          @RequestParam(required = false) com.example.tournament.entity.MatchStatus status,
                          @RequestParam(required = false) com.example.tournament.entity.MatchPhase phase,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model) {

        Tournament tournament = getTournament(id);
        var result = matchService.search(id, status, phase, null, null, null, null,
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("roundNumber").ascending()
                                .and(org.springframework.data.domain.Sort.by("bracketPosition").ascending())));
        model.addAttribute("tournament", tournament);
        model.addAttribute("matches", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("statusFilter", status);
        model.addAttribute("phaseFilter", phase);
        model.addAttribute("statuses", com.example.tournament.entity.MatchStatus.values());
        model.addAttribute("phases", com.example.tournament.entity.MatchPhase.values());
        return "tournament/matches";
    }

    @GetMapping("/ranking")
    public String ranking(@PathVariable Long id, Model model) {
        Tournament tournament = getTournament(id);
        model.addAttribute("tournament", tournament);
        model.addAttribute("standings", rankingService.getStandings(id));
        model.addAttribute("criteria", tournament.getRankingCriteria());
        return "tournament/ranking";
    }

    @GetMapping("/bracket")
    public String bracket(@PathVariable Long id, Model model) {
        Tournament tournament = getTournament(id);
        model.addAttribute("tournament", tournament);
        model.addAttribute("bracket", rankingService.buildBracket(id));
        return "tournament/bracket";
    }

    @GetMapping("/statistics")
    public String statistics(@PathVariable Long id, Model model) {
        Tournament tournament = getTournament(id);
        model.addAttribute("tournament", tournament);
        model.addAttribute("stats", statisticsService.tournamentStatistics(id));
        return "tournament/statistics";
    }

    @GetMapping("/generate")
    public String generateForm(@PathVariable Long id, Model model) {
        model.addAttribute("generationForm",
                com.example.tournament.dto.request.GenerateMatchesForm.from(getTournament(id)));
        return populateGenerate(id, model);
    }

    @PostMapping("/generate")
    public String generate(@PathVariable Long id,
                           @Valid @ModelAttribute("generationForm") com.example.tournament.dto.request.GenerateMatchesForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return populateGenerate(id, model);
        }
        var result = matchService.generateMatches(id, form.toRequest());
        redirectAttributes.addFlashAttribute("success",
                result.generatedMatches() + " rencontres générées sur " + result.rounds() + " tour(s).");
        return "redirect:/tournaments/" + id + "/matches";
    }

    private String populateGenerate(Long id, Model model) {
        Tournament tournament = getTournament(id);
        long registered = tournamentService.getRegistrations(id).stream()
                .filter(r -> r.getStatus() == RegistrationStatus.INSCRITE)
                .count();
        model.addAttribute("tournament", tournament);
        model.addAttribute("registeredCount", registered);
        model.addAttribute("hasMatches", matchService.findByTournament(id).stream().findAny().isPresent());
        return "tournament/generate";
    }

    private Tournament getTournament(Long id) {
        return tournamentService.findById(id)
                .orElseThrow(() -> new com.example.tournament.exception.ResourceNotFoundException(
                        "Tournoi introuvable : " + id));
    }
}
