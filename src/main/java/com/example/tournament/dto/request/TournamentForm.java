package com.example.tournament.dto.request;

import com.example.tournament.entity.RankingCriteria;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulaire de création / modification d'un tournoi (application MVC).
 * Classe mutable afin de pouvoir être liée par Spring MVC.
 */
public class TournamentForm {

    private Long id;

    @NotBlank(message = "Le nom du tournoi est obligatoire")
    @Size(max = 120, message = "Le nom ne doit pas dépasser 120 caractères")
    private String name;

    @NotNull(message = "Le sport est obligatoire")
    private Sport sport = Sport.FOOTBALL;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate = LocalDate.now().plusWeeks(2);

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Size(max = 200)
    private String venue;

    @NotNull(message = "Le nombre maximum d'équipes est obligatoire")
    @Min(value = 2, message = "Au moins 2 équipes")
    @Max(value = 256)
    private Integer maxTeams = 16;

    @NotNull(message = "Le type de tournoi est obligatoire")
    private TournamentType type = TournamentType.CHAMPIONNAT_SIMPLE;

    @NotNull(message = "Le statut est obligatoire")
    private TournamentStatus status = TournamentStatus.BROUILLON;

    @Min(value = 1, message = "Minimum 1 point")
    @Max(value = 10)
    private Integer pointsForWin = 3;

    @Min(value = 0, message = "Minimum 0 point")
    @Max(value = 10)
    private Integer pointsForDraw = 1;

    private List<RankingCriteria> rankingCriteria = new ArrayList<>(
            List.of(RankingCriteria.POINTS, RankingCriteria.GOAL_DIFFERENCE, RankingCriteria.GOALS_SCORED));

    @Positive(message = "Valeur positive attendue")
    private Integer daysBetweenRounds = 7;

    @PositiveOrZero
    private Integer matchDurationMinutes = 90;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Heure attendue au format HH:mm")
    private String kickoffTime = "15:00";

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

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Integer getMaxTeams() {
        return maxTeams;
    }

    public void setMaxTeams(Integer maxTeams) {
        this.maxTeams = maxTeams;
    }

    public TournamentType getType() {
        return type;
    }

    public void setType(TournamentType type) {
        this.type = type;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public Integer getPointsForWin() {
        return pointsForWin;
    }

    public void setPointsForWin(Integer pointsForWin) {
        this.pointsForWin = pointsForWin;
    }

    public Integer getPointsForDraw() {
        return pointsForDraw;
    }

    public void setPointsForDraw(Integer pointsForDraw) {
        this.pointsForDraw = pointsForDraw;
    }

    public List<RankingCriteria> getRankingCriteria() {
        return rankingCriteria;
    }

    public void setRankingCriteria(List<RankingCriteria> rankingCriteria) {
        this.rankingCriteria = rankingCriteria;
    }

    public Integer getDaysBetweenRounds() {
        return daysBetweenRounds;
    }

    public void setDaysBetweenRounds(Integer daysBetweenRounds) {
        this.daysBetweenRounds = daysBetweenRounds;
    }

    public Integer getMatchDurationMinutes() {
        return matchDurationMinutes;
    }

    public void setMatchDurationMinutes(Integer matchDurationMinutes) {
        this.matchDurationMinutes = matchDurationMinutes;
    }

    public String getKickoffTime() {
        return kickoffTime;
    }

    public void setKickoffTime(String kickoffTime) {
        this.kickoffTime = kickoffTime;
    }

    // ------------------------------------------------- Conversion vers la couche API

    public TournamentRequest toCreateRequest() {
        return new TournamentRequest(name, sport, description, startDate, endDate, venue, maxTeams, type,
                pointsForWin, pointsForDraw, rankingCriteria, daysBetweenRounds, matchDurationMinutes, kickoffTime);
    }

    public TournamentUpdateRequest toUpdateRequest() {
        return new TournamentUpdateRequest(name, description, startDate, endDate, venue, maxTeams, status,
                pointsForWin, pointsForDraw, rankingCriteria, daysBetweenRounds, matchDurationMinutes, kickoffTime);
    }

    public static TournamentForm from(com.example.tournament.entity.Tournament tournament) {
        TournamentForm form = new TournamentForm();
        form.setId(tournament.getId());
        form.setName(tournament.getName());
        form.setSport(tournament.getSport());
        form.setDescription(tournament.getDescription());
        form.setStartDate(tournament.getStartDate());
        form.setEndDate(tournament.getEndDate());
        form.setVenue(tournament.getVenue());
        form.setMaxTeams(tournament.getMaxTeams());
        form.setType(tournament.getType());
        form.setStatus(tournament.getStatus());
        form.setPointsForWin(tournament.getPointsForWin());
        form.setPointsForDraw(tournament.getPointsForDraw());
        form.setRankingCriteria(new ArrayList<>(tournament.getRankingCriteria()));
        form.setDaysBetweenRounds(tournament.getDaysBetweenRounds());
        form.setMatchDurationMinutes(tournament.getMatchDurationMinutes());
        form.setKickoffTime(tournament.getKickoffTime());
        return form;
    }
}
