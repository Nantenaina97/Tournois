package com.example.tournament.dto.request;

import com.example.tournament.entity.MatchStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/** Payload de saisie / modification du résultat d'un match. */
public record MatchResultRequest(

        @NotNull(message = "Le score de l'équipe à domicile est obligatoire")
        @Min(value = 0, message = "Le score ne peut pas être négatif")
        Integer homeScore,

        @NotNull(message = "Le score de l'équipe à l'extérieur est obligatoire")
        @Min(value = 0, message = "Le score ne peut pas être négatif")
        Integer awayScore,

        /** Équipe gagnante en cas d'égalité (tirs au but). */
        @Positive(message = "L'identifiant du vainqueur doit être positif")
        Long winnerTeamId,

        @Size(max = 120, message = "Le terrain ne doit pas dépasser 120 caractères")
        String field,

        LocalDate matchDate,

        LocalTime matchTime,

        MatchStatus status) {
}
