package com.example.tournament.entity;

/**
 * Cycle de vie d'un tournoi.
 *
 * <pre>
 *  BROUILLON ──▶ INSCRIPTION ──▶ PROGRAMME ──▶ EN_COURS ──▶ TERMINE
 *      │              │               │             │
 *      └──────────────┴───────────────┴─────────────┴──▶ ANNULE
 * </pre>
 */
public enum TournamentStatus {

    BROUILLON("Brouillon", false),
    INSCRIPTION("Inscriptions ouvertes", false),
    PROGRAMME("Programmé", false),
    EN_COURS("En cours", false),
    TERMINE("Terminé", true),
    ANNULE("Annulé", true);

    private final String libelle;
    private final boolean closed;

    TournamentStatus(String libelle, boolean closed) {
        this.libelle = libelle;
        this.closed = closed;
    }

    public String getLibelle() {
        return libelle;
    }

    /** {@code true} si aucune modification métier n'est plus autorisée. */
    public boolean isClosed() {
        return closed;
    }

    /** {@code true} si l'on peut encore inscrire des équipes. */
    public boolean isRegistrationOpen() {
        return this == BROUILLON || this == INSCRIPTION;
    }

    /** {@code true} si des résultats peuvent être saisis. */
    public boolean isMatchPlayable() {
        return this == PROGRAMME || this == EN_COURS;
    }
}
