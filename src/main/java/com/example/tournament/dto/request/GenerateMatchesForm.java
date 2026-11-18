package com.example.tournament.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Formulaire de génération du calendrier (application MVC).
 *
 * <p>Classe mutable : contrairement au record {@link GenerateMatchesRequest} utilisé par l'API,
 * elle se lie correctement aux champs de formulaire Thymeleaf.</p>
 */
public class GenerateMatchesForm {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Positive(message = "Le nombre de jours entre deux tours doit être positif")
    private Integer daysBetweenRounds = 7;

    @PositiveOrZero(message = "Le nombre de terrains doit être positif")
    private Integer numberOfFields = 4;

    @Positive(message = "L'heure de début doit être valide")
    private Integer kickoffHour;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getDaysBetweenRounds() {
        return daysBetweenRounds;
    }

    public void setDaysBetweenRounds(Integer daysBetweenRounds) {
        this.daysBetweenRounds = daysBetweenRounds;
    }

    public Integer getNumberOfFields() {
        return numberOfFields;
    }

    public void setNumberOfFields(Integer numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public Integer getKickoffHour() {
        return kickoffHour;
    }

    public void setKickoffHour(Integer kickoffHour) {
        this.kickoffHour = kickoffHour;
    }

    public GenerateMatchesRequest toRequest() {
        return new GenerateMatchesRequest(startDate, daysBetweenRounds, numberOfFields, kickoffHour);
    }

    public static GenerateMatchesForm from(TournamentRequest tournament) {
        GenerateMatchesForm form = new GenerateMatchesForm();
        form.setStartDate(tournament.startDate());
        form.setDaysBetweenRounds(tournament.daysBetweenRounds());
        form.setNumberOfFields(4);
        form.setKickoffHour(null);
        return form;
    }

    public static GenerateMatchesForm from(com.example.tournament.entity.Tournament tournament) {
        GenerateMatchesForm form = new GenerateMatchesForm();
        form.setStartDate(tournament.getStartDate());
        form.setDaysBetweenRounds(tournament.getDaysBetweenRounds());
        form.setNumberOfFields(4);
        form.setKickoffHour(null);
        return form;
    }
}
