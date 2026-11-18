package com.example.tournament.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Inscription d'une équipe à un tournoi.
 *
 * <p>Entité d'association porteuse de données : tête de série, date d'inscription, statut.
 * La contrainte d'unicité {@code (tournament_id, team_id)} est garantie au niveau du schéma,
 * ce qui empêche structurellement la double inscription d'une même équipe.</p>
 */
@Entity
@Table(name = "team_registrations",
        uniqueConstraints = @UniqueConstraint(name = "uk_registration_tournament_team",
                columnNames = {"tournament_id", "team_id"}))
public class TeamRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Tête de série (1 = meilleure). Null tant que le tirage n'est pas effectué. */
    @Column(name = "seed_number")
    private Integer seedNumber;

    @Column(name = "registration_date", nullable = false)
    private Instant registrationDate = Instant.now();

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.INSCRITE;

    public TeamRegistration() {
    }

    public TeamRegistration(Tournament tournament, Team team, Integer seedNumber) {
        this.tournament = tournament;
        this.team = team;
        this.seedNumber = seedNumber;
    }

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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Integer getSeedNumber() {
        return seedNumber;
    }

    public void setSeedNumber(Integer seedNumber) {
        this.seedNumber = seedNumber;
    }

    public Instant getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Instant registrationDate) {
        this.registrationDate = registrationDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}
