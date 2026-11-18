package com.example.tournament.entity;

/** Statut d'une équipe. */
public enum TeamStatus {

    ACTIF("Actif"),
    INACTIF("Inactif"),
    ELIMINE("Éliminé"),
    DISQUALIFIE("Disqualifié");

    private final String libelle;

    TeamStatus(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
