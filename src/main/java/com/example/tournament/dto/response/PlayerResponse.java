package com.example.tournament.dto.response;

/** Représentation d'un joueur. */
public record PlayerResponse(
        Long id,
        Long teamId,
        String firstName,
        String lastName,
        String fullName,
        String position,
        Integer jerseyNumber,
        String nationality) {
}
