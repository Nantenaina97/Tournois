package com.example.tournament.dto.request;

import com.example.tournament.entity.TournamentStatus;
import jakarta.validation.constraints.NotNull;

/** Payload de changement de statut d'un tournoi. */
public record StatusChangeRequest(
        @NotNull(message = "Le nouveau statut est obligatoire")
        TournamentStatus status) {
}
