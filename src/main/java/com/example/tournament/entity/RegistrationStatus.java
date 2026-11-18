package com.example.tournament.entity;

/** Statut d'une inscription d'équipe à un tournoi. */
public enum RegistrationStatus {

    INSCRITE("Inscrite"),
    RETIREE("Retirée"),
    ELIMINEE("Éliminée"),
    CHAMPIONNE("Championne");

    private final String libelle;

    RegistrationStatus(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
