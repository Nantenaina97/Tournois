package com.example.tournament.dto.response;

import com.example.tournament.entity.RegistrationStatus;

import java.time.Instant;

/** Représentation d'une inscription d'équipe à un tournoi. */
public record RegistrationResponse(
        Long id,
        Long tournamentId,
        String tournamentName,
        Long teamId,
        String teamName,
        String teamCity,
        String teamLogoUrl,
        Integer seedNumber,
        RegistrationStatus status,
        String statusLabel,
        Instant registrationDate) {
}
