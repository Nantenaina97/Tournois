package com.example.tournament.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/** Payload de génération automatique des rencontres. */
public record GenerateMatchesRequest(

        /** Date de la première rencontre. Par défaut : date de début du tournoi. */
        @FutureOrPresent(message = "La date de début du calendrier ne peut pas être dans le passé")
        LocalDate startDate,

        @Positive(message = "Le nombre de jours entre deux tours doit être positif")
        Integer daysBetweenRounds,

        @PositiveOrZero(message = "Le nombre de terrains doit être positif")
        Integer numberOfFields,

        @Positive(message = "L'heure de début doit être valide")
        Integer kickoffHour) {

    public LocalDate effectiveStartDate(LocalDate tournamentStart) {
        return startDate != null ? startDate : tournamentStart;
    }
}
