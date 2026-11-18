package com.example.tournament.dto.request;

import com.example.tournament.entity.TeamStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Payload de création / modification d'une équipe. */
public record TeamRequest(

        @NotBlank(message = "Le nom de l'équipe est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String name,

        @Pattern(regexp = "^(https?://).+$", message = "L'URL du logo doit commencer par http:// ou https://")
        @Size(max = 500)
        String logoUrl,

        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String city,

        @Size(max = 100, message = "Le nom de l'entraîneur ne doit pas dépasser 100 caractères")
        String coach,

        @Pattern(regexp = "^[+0-9 ().-]{6,30}$", message = "Le numéro de téléphone est invalide")
        String coachPhone,

        @Min(value = 1800, message = "L'année de fondation est invalide")
        @Max(value = 2100, message = "L'année de fondation est invalide")
        Integer foundedYear,

        TeamStatus status) {
}
