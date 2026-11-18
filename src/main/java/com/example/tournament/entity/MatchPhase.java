package com.example.tournament.entity;

/** Phase (tour) d'un match. */
public enum MatchPhase {

    TOUR_PRELIMINAIRE("Tour préliminaire", 0),
    SEIZIEME_FINALE("Huitièmes de finale", 1),
    QUART_FINALE("Quart de finale", 2),
    DEMI_FINALE("Demi-finale", 3),
    FINALE("Finale", 4),
    CHAMPIONNAT("Championnat", 0),
    PHASE_GRUPE("Phase de groupes", 0),
    MATCH_3E_PLACE("Match pour la 3e place", 5);

    private final String libelle;
    private final int ordre;

    MatchPhase(String libelle, int ordre) {
        this.libelle = libelle;
        this.ordre = ordre;
    }

    public String getLibelle() {
        return libelle;
    }

    public int getOrdre() {
        return ordre;
    }
}
