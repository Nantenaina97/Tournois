package com.example.tournament.entity;

/** Statut d'un match. */
public enum MatchStatus {

    PLANIFIE("Planifié"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    REPORTE("Reporté"),
    ANNULE("Annulé");

    private final String libelle;

    MatchStatus(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    /** Un match « joué » est un match terminé avec un score. */
    public boolean isFinishedWithScore() {
        return this == TERMINE;
    }
}
