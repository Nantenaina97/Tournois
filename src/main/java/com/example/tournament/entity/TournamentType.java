package com.example.tournament.entity;

/** Format de compétition. */
public enum TournamentType {

    CHAMPIONNAT_SIMPLE("Championnat (tournoi simple)", false),
    CHAMPIONNAT_ALLER_RETOUR("Championnat aller-retour", false),
    ELIMINATION_DIRECTE("Élimination directe", true);

    private final String libelle;
    private final boolean knockout;

    TournamentType(String libelle, boolean knockout) {
        this.libelle = libelle;
        this.knockout = knockout;
    }

    public String getLibelle() {
        return libelle;
    }

    /** {@code true} si le format est une élimination directe (bracket). */
    public boolean isKnockout() {
        return knockout;
    }
}
