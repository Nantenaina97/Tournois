package com.example.tournament.controller.web;

import com.example.tournament.dto.request.TeamForm;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.service.PlayerService;
import com.example.tournament.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

/** Gestion des équipes dans l'interface MVC. */
@Controller
@RequestMapping("/teams")
public class TeamWebController {

    private final TeamService teamService;
    private final PlayerService playerService;

    public TeamWebController(TeamService teamService, PlayerService playerService) {
        this.teamService = teamService;
        this.playerService = playerService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) TeamStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "name,asc") String sort,
                       Model model) {

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<Team> result = teamService.search(search, status, null, pageable);
        model.addAttribute("teams", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", TeamStatus.values());
        model.addAttribute("sort", sort);
        return "team/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("teamForm", new TeamForm());
        model.addAttribute("statuses", TeamStatus.values());
        return "team/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("teamForm") TeamForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", TeamStatus.values());
            return "team/form";
        }
        Team created = teamService.create(form.toRequest());
        return "redirect:/teams/" + created.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Team team = teamService.findById(id)
                .orElseThrow(() -> new com.example.tournament.exception.ResourceNotFoundException(
                        "Équipe introuvable : " + id));
        model.addAttribute("teamForm", TeamForm.from(team));
        model.addAttribute("statuses", TeamStatus.values());
        return "team/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("teamForm") TeamForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            form.setId(id);
            model.addAttribute("statuses", TeamStatus.values());
            return "team/form";
        }
        teamService.update(id, form.toRequest());
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        teamService.delete(id);
        redirectAttributes.addFlashAttribute("success", "L'équipe a été supprimée.");
        return "redirect:/teams";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Team team = teamService.findById(id)
                .orElseThrow(() -> new com.example.tournament.exception.ResourceNotFoundException(
                        "Équipe introuvable : " + id));
        model.addAttribute("team", team);
        model.addAttribute("players", playerService.findByTeam(id));
        model.addAttribute("history", teamService.getHistory(id));
        return "team/detail";
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0].trim());
    }
}
