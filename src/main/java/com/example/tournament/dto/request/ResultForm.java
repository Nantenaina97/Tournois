package com.example.tournament.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/** Formulaire de saisie du résultat d'un match (application MVC). */
public class ResultForm {

    @NotNull(message = "Le score à domicile est obligatoire")
    @Min(value = 0, message = "Score positif attendu")
    private Integer homeScore;

    @NotNull(message = "Le score à l'extérieur est obligatoire")
    @Min(value = 0, message = "Score positif attendu")
    private Integer awayScore;

    private Long winnerTeamId;

    @Size(max = 120)
    private String field;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate matchDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime matchTime;

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public Long getWinnerTeamId() {
        return winnerTeamId;
    }

    public void setWinnerTeamId(Long winnerTeamId) {
        this.winnerTeamId = winnerTeamId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public LocalTime getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(LocalTime matchTime) {
        this.matchTime = matchTime;
    }

    public MatchResultRequest toRequest() {
        return new MatchResultRequest(homeScore, awayScore, winnerTeamId, field, matchDate, matchTime, null);
    }
}
