package com.example.tournament.controller.web;

import com.example.tournament.dto.request.TournamentForm;
import com.example.tournament.dto.request.TournamentUpdateRequest;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.service.TournamentService;
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

/** Gestion des tournois dans l'interface MVC. */
@Controller
@RequestMapping("/tournaments")
public class TournamentWebController {

    private final TournamentService tournamentService;
    private final com.example.tournament.mapper.TournamentMapper mapper;

    public TournamentWebController(TournamentService tournamentService,
                                   com.example.tournament.mapper.TournamentMapper mapper) {
        this.tournamentService = tournamentService;
        this.mapper = mapper;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) TournamentStatus status,
                       @RequestParam(required = false) TournamentType type,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "startDate,desc") String sort,
                       Model model) {

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<Tournament> result = tournamentService.search(search, status, type, null, null, null, pageable);

        model.addAttribute("tournaments",
                result.getContent().stream().map(mapper::toResponse).toList());
        model.addAttribute("page", result);
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", status);
        model.addAttribute("typeFilter", type);
        model.addAttribute("sort", sort);
        model.addAttribute("statuses", TournamentStatus.values());
        model.addAttribute("types", TournamentType.values());
        model.addAttribute("sports", Sport.values());
        return "tournament/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("tournamentForm", new TournamentForm());
        return populateForm(model, "tournament/form");
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("tournamentForm") TournamentForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return populateForm(model, "tournament/form");
        }
        Tournament created = tournamentService.create(form.toCreateRequest());
        return "redirect:/tournaments/" + created.getId() + "/teams";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> new com.example.tournament.exception.ResourceNotFoundException(
                        "Tournoi introuvable : " + id));
        model.addAttribute("tournamentForm", TournamentForm.from(tournament));
        return populateForm(model, "tournament/form");
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("tournamentForm") TournamentForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            form.setId(id);
            return populateForm(model, "tournament/form");
        }
        TournamentUpdateRequest request = form.toUpdateRequest();
        Tournament updated = tournamentService.update(id, request);
        return "redirect:/tournaments/" + updated.getId();
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam TournamentStatus status,
                               RedirectAttributes redirectAttributes) {
        tournamentService.changeStatus(id, status);
        redirectAttributes.addFlashAttribute("success",
                "Statut du tournoi mis à jour : " + status.getLibelle() + ".");
        return "redirect:/tournaments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        tournamentService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Le tournoi a été supprimé.");
        return "redirect:/tournaments";
    }

    private String populateForm(Model model, String view) {
        model.addAttribute("statuses", TournamentStatus.values());
        model.addAttribute("types", TournamentType.values());
        model.addAttribute("sports", Sport.values());
        model.addAttribute("criteria", com.example.tournament.entity.RankingCriteria.values());
        return view;
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "startDate");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, parts[0].trim());
    }
}
