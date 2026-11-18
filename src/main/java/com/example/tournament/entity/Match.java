package com.example.tournament.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Une rencontre entre deux équipes.
 *
 * <p>Une équipe peut jouer {@code 2 * maxTeams} matchs : les colonnes {@code home_team_id} et
 * {@code away_team_id} sont indexées afin de détecter rapidement les conflits d'horaire.</p>
 *
 * <p>En élimination directe, un match peut être un « exempt » (bye) : une des deux équipes est
 * alors {@code null} et l'équipe présente est directement qualifiée.</p>
 */
@Entity
@Table(name = "matches",
        indexes = {
                @Index(name = "idx_match_tournament", columnList = "tournament_id"),
                @Index(name = "idx_match_date", columnList = "match_date"),
                @Index(name = "idx_match_home", columnList = "home_team_id"),
                @Index(name = "idx_match_away", columnList = "away_team_id")
        })
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    /** Libellé de remplacement quand l'équipe n'est pas encore connue (ex. « Vainqueur QF1 »). */
    @Column(name = "home_label", length = 120)
    private String homeLabel;

    @Column(name = "away_label", length = 120)
    private String awayLabel;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "match_time")
    private LocalTime matchTime;

    /** Terrain / lieu de la rencontre. */
    @Column(length = 120)
    private String field;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchPhase phase = MatchPhase.CHAMPIONNAT;

    /** Numéro de journée (championnat) ou de tour (élimination directe). */
    @Column(name = "round_number", nullable = false)
    private Integer roundNumber = 1;

    /** Position du match dans le tableau d'élimination directe (0-based). */
    @Column(name = "bracket_position")
    private Integer bracketPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.PLANIFIE;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    /** Libellé du tour tel qu'affiché (Huitièmes, Quarts, ...). */
    @Column(name = "phase_label", length = 60)
    private String phaseLabel;

    /** Match de barrage / exempt : aucun score ne sera saisi. */
    @Column(nullable = false)
    private boolean bye = false;

    /** Vainqueur désigné (tirs au but / prolongation) lorsque le score est à égalité. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winner;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getHomeLabel() {
        return homeLabel;
    }

    public void setHomeLabel(String homeLabel) {
        this.homeLabel = homeLabel;
    }

    public String getAwayLabel() {
        return awayLabel;
    }

    public void setAwayLabel(String awayLabel) {
        this.awayLabel = awayLabel;
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

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public MatchPhase getPhase() {
        return phase;
    }

    public void setPhase(MatchPhase phase) {
        this.phase = phase;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Integer getBracketPosition() {
        return bracketPosition;
    }

    public void setBracketPosition(Integer bracketPosition) {
        this.bracketPosition = bracketPosition;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

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

    public String getPhaseLabel() {
        return phaseLabel;
    }

    public void setPhaseLabel(String phaseLabel) {
        this.phaseLabel = phaseLabel;
    }

    public boolean isBye() {
        return bye;
    }

    public void setBye(boolean bye) {
        this.bye = bye;
    }

    public Team getWinner() {
        return winner;
    }

    public void setWinner(Team winner) {
        this.winner = winner;
    }

    public Long getVersion() {
        return version;
    }

    /** Date et heure complètes de la rencontre. */
    public LocalDateTime getKickoff() {
        if (matchDate == null) {
            return null;
        }
        return LocalDateTime.of(matchDate, matchTime == null ? LocalTime.MIDNIGHT : matchTime);
    }

    /** {@code true} si les deux équipes sont connues. */
    public boolean isReady() {
        return homeTeam != null && awayTeam != null;
    }

    /** {@code true} si un score a été saisi. */
    public boolean hasScore() {
        return homeScore != null && awayScore != null;
    }
}
