package com.example.tournament.dto.request;

import com.example.tournament.entity.RankingCriteria;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.TournamentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** Payload de création d'un tournoi. */
public record TournamentRequest(

        @NotBlank(message = "Le nom du tournoi est obligatoire")
        @Size(max = 120, message = "Le nom ne doit pas dépasser 120 caractères")
        String name,

        @NotNull(message = "Le sport est obligatoire")
        Sport sport,

        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description,

        @NotNull(message = "La date de début est obligatoire")
        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 200, message = "Le lieu ne doit pas dépasser 200 caractères")
        String venue,

        @NotNull(message = "Le nombre maximum d'équipes est obligatoire")
        @Min(value = 2, message = "Un tournoi doit accueillir au moins 2 équipes")
        @Max(value = 256, message = "Le nombre maximum d'équipes est limité à 256")
        Integer maxTeams,

        @NotNull(message = "Le type de tournoi est obligatoire")
        TournamentType type,

        @Min(value = 1, message = "Les points par victoire doivent être positifs")
        @Max(value = 10, message = "Les points par victoire ne peuvent pas dépasser 10")
        Integer pointsForWin,

        @Min(value = 0, message = "Les points par nul ne peuvent pas être négatifs")
        @Max(value = 10, message = "Les points par nul ne peuvent pas dépasser 10")
        Integer pointsForDraw,

        List<RankingCriteria> rankingCriteria,

        @Positive(message = "Le nombre de jours entre deux tours doit être positif")
        Integer daysBetweenRounds,

        @PositiveOrZero(message = "La durée d'un match ne peut pas être négative")
        Integer matchDurationMinutes,

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "L'heure doit être au format HH:mm")
        String kickoffTime) {

    @AssertTrue(message = "La date de fin doit être postérieure ou égale à la date de début")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
