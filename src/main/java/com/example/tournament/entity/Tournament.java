package com.example.tournament.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Un tournoi sportif.
 *
 * <p>Entité racine de l'agrégat « compétition » : elle porte les équipes inscrites,
 * les matchs générés et le classement calculé.</p>
 */
@Entity
@Table(name = "tournaments")
@EntityListeners(AuditingEntityListener.class)
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Sport sport = Sport.FOOTBALL;

    @Column(length = 2000)
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 200)
    private String venue;

    @Column(name = "max_teams", nullable = false)
    private Integer maxTeams = 16;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TournamentType type = TournamentType.CHAMPIONNAT_SIMPLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TournamentStatus status = TournamentStatus.BROUILLON;

    /** Barème : points gagnés pour une victoire. */
    @Column(name = "points_for_win", nullable = false)
    private Integer pointsForWin = 3;

    /** Barème : points gagnés pour un match nul. */
    @Column(name = "points_for_draw", nullable = false)
    private Integer pointsForDraw = 1;

    /** Liste ordonnée des critères de classement (le 1er est prioritaire). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tournament_ranking_criteria",
            joinColumns = @JoinColumn(name = "tournament_id"))
    @OrderColumn(name = "criteria_order")
    @Column(name = "criteria", length = 40, nullable = false)
    @Enumerated(EnumType.STRING)
    private List<RankingCriteria> rankingCriteria = new ArrayList<>(
            List.of(RankingCriteria.POINTS, RankingCriteria.GOAL_DIFFERENCE, RankingCriteria.GOALS_SCORED));

    /** jours séparant deux journées (championnat) ou deux tours (élimination directe). */
    @Column(name = "days_between_rounds", nullable = false)
    private Integer daysBetweenRounds = 7;

    /** Heure de début des rencontres. */
    @Column(name = "kickoff_time", length = 5)
    private String kickoffTime = "15:00";

    @Column(name = "match_duration_minutes", nullable = false)
    private Integer matchDurationMinutes = 90;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

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

    public String getKickoffTime() {
        return kickoffTime;
    }

    public void setKickoffTime(String kickoffTime) {
        this.kickoffTime = kickoffTime;
    }

    public Integer getMatchDurationMinutes() {
        return matchDurationMinutes;
    }

    public void setMatchDurationMinutes(Integer matchDurationMinutes) {
        this.matchDurationMinutes = matchDurationMinutes;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
