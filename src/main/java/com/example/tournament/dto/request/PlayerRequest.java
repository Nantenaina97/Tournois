package com.example.tournament.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Payload de création / modification d'un joueur. */
public record PlayerRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 60)
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 60)
        String lastName,

        @Size(max = 40, message = "Le poste ne doit pas dépasser 40 caractères")
        String position,

        @Min(value = 1, message = "Le numéro de maillot doit être compris entre 1 et 99")
        @Max(value = 99, message = "Le numéro de maillot doit être compris entre 1 et 99")
        Integer jerseyNumber,

        @PastOrPresent(message = "La date de naissance ne peut pas être dans le futur")
        LocalDate birthDate,

        @Size(max = 20)
        String nationality) {
}
