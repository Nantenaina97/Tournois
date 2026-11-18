package com.example.tournament.controller.web;

import com.example.tournament.entity.Role;
import com.example.tournament.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Gestion des utilisateurs (réservée au rôle ADMIN). */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserWebController {

    private final UserService userService;

    public AdminUserWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping
    public String create(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam(required = false) String fullName,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) Role role,
                         RedirectAttributes redirectAttributes) {
        userService.create(username, password, fullName, email, role);
        redirectAttributes.addFlashAttribute("success", "Utilisateur « " + username + " » créé.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@RequestParam Long id,
                             @RequestParam Role role,
                             RedirectAttributes redirectAttributes) {
        userService.changeRole(id, role);
        redirectAttributes.addFlashAttribute("success", "Rôle mis à jour.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        userService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé.");
        return "redirect:/admin/users";
    }
}
