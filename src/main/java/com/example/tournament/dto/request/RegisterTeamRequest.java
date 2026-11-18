package com.example.tournament.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Payload d'inscription d'une équipe à un tournoi. */
public record RegisterTeamRequest(

        @NotNull(message = "L'identifiant de l'équipe est obligatoire")
        @Positive(message = "L'identifiant de l'équipe doit être positif")
        Long teamId,

        @Min(value = 1, message = "La tête de série doit être supérieure ou égale à 1")
        Integer seedNumber) {
}
