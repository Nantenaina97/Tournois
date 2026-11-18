package com.example.tournament.dto.response;

import com.example.tournament.entity.Role;

/** Représentation d'un utilisateur. */
public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Role role,
        String roleLabel,
        boolean enabled) {
}
