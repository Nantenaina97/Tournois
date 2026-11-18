package com.example.tournament.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Formulaire d'inscription d'une équipe à un tournoi. */
public class RegistrationForm {

    @NotNull(message = "Sélectionnez une équipe")
    @Positive(message = "Équipe invalide")
    private Long teamId;

    @Min(value = 1, message = "La tête de série doit être ≥ 1")
    private Integer seedNumber;

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Integer getSeedNumber() {
        return seedNumber;
    }

    public void setSeedNumber(Integer seedNumber) {
        this.seedNumber = seedNumber;
    }
}
