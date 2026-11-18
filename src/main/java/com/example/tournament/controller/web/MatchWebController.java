package com.example.tournament.controller.web;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.dto.request.ResultForm;
import com.example.tournament.entity.Match;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Saisie des résultats et gestion des joueurs dans l'interface MVC. */
@Controller
public class MatchWebController {

    private final MatchService matchService;
    private final PlayerService playerService;

    public MatchWebController(MatchService matchService, PlayerService playerService) {
        this.matchService = matchService;
        this.playerService = playerService;
    }

    @GetMapping("/matches/{id}/result")
    public String resultForm(@PathVariable Long id, Model model) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> new com.example.tournament.exception.ResourceNotFoundException(
                        "Match introuvable : " + id));

        ResultForm form = new ResultForm();
        form.setHomeScore(match.getHomeScore() == null ? 0 : match.getHomeScore());
        form.setAwayScore(match.getAwayScore() == null ? 0 : match.getAwayScore());
        form.setField(match.getField());
        form.setMatchDate(match.getMatchDate());
        form.setMatchTime(match.getMatchTime());
        form.setWinnerTeamId(match.getWinner() != null ? match.getWinner().getId() : null);

        model.addAttribute("match", match);
        model.addAttribute("resultForm", form);
        model.addAttribute("tournament", match.getTournament());
        return "match/result";
    }

    @PostMapping("/matches/{id}/result")
    public String saveResult(@PathVariable Long id,
                             @Valid @ModelAttribute("resultForm") ResultForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Match match = matchService.findById(id)
                    .orElseThrow(() -> new com.example.tournament.exception.ResourceNotFoundException(
                            "Match introuvable : " + id));
            model.addAttribute("match", match);
            model.addAttribute("tournament", match.getTournament());
            return "match/result";
        }
        Match match = matchService.saveResult(id, form.toRequest());
        Long tournamentId = match.getTournament().getId();
        redirectAttributes.addFlashAttribute("success",
                "Résultat enregistré : " + match.getHomeScore() + " - " + match.getAwayScore() + ".");
        return "redirect:/tournaments/" + tournamentId + "/matches";
    }

    @PostMapping("/teams/{teamId}/players")
    public String addPlayer(@PathVariable Long teamId,
                            @Valid @ModelAttribute("playerRequest") PlayerRequest request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Le formulaire du joueur est incomplet ou invalide.");
            return "redirect:/teams/" + teamId;
        }
        playerService.addPlayer(teamId, request);
        redirectAttributes.addFlashAttribute("success", "Le joueur a été ajouté à l'effectif.");
        return "redirect:/teams/" + teamId;
    }

    @PostMapping("/teams/{teamId}/players/{playerId}/delete")
    public String deletePlayer(@PathVariable Long teamId, @PathVariable Long playerId,
                               RedirectAttributes redirectAttributes) {
        playerService.removePlayer(teamId, playerId);
        redirectAttributes.addFlashAttribute("success", "Le joueur a été retiré de l'effectif.");
        return "redirect:/teams/" + teamId;
    }
}
