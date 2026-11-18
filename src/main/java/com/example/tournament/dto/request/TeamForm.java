package com.example.tournament.dto.request;

import com.example.tournament.entity.TeamStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Formulaire de création / modification d'une équipe (application MVC). */
public class TeamForm {

    private Long id;

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @Pattern(regexp = "^(https?://).+$", message = "URL attendue (http:// ou https://)")
    @Size(max = 500)
    private String logoUrl;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String coach;

    @Pattern(regexp = "^[+0-9 ().-]{6,30}$", message = "Numéro de téléphone invalide")
    private String coachPhone;

    @Min(value = 1800, message = "Année invalide")
    @Max(value = 2100, message = "Année invalide")
    private Integer foundedYear;

    private TeamStatus status = TeamStatus.ACTIF;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCoach() {
        return coach;
    }

    public void setCoach(String coach) {
        this.coach = coach;
    }

    public String getCoachPhone() {
        return coachPhone;
    }

    public void setCoachPhone(String coachPhone) {
        this.coachPhone = coachPhone;
    }

    public Integer getFoundedYear() {
        return foundedYear;
    }

    public void setFoundedYear(Integer foundedYear) {
        this.foundedYear = foundedYear;
    }

    public TeamStatus getStatus() {
        return status;
    }

    public void setStatus(TeamStatus status) {
        this.status = status;
    }

    public TeamRequest toRequest() {
        return new TeamRequest(name, logoUrl, city, coach, coachPhone, foundedYear, status);
    }

    public static TeamForm from(com.example.tournament.entity.Team team) {
        TeamForm form = new TeamForm();
        form.setId(team.getId());
        form.setName(team.getName());
        form.setLogoUrl(team.getLogoUrl());
        form.setCity(team.getCity());
        form.setCoach(team.getCoach());
        form.setCoachPhone(team.getCoachPhone());
        form.setFoundedYear(team.getFoundedYear());
        form.setStatus(team.getStatus());
        return form;
    }
}
