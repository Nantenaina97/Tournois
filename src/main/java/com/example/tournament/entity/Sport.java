package com.example.tournament.entity;

/** Sports gérés par la plateforme. */
public enum Sport {

    FOOTBALL("Football", true),
    BASKETBALL("Basketball", true),
    HANDBALL("Handball", true),
    VOLLEYBALL("Volley-ball", true),
    RUGBY("Rugby", true),
    TENNIS("Tennis", false),
    BADMINTON("Badminton", false),
    ATHLETICS("Athlétisme", false),
    SWIMMING("Natation", false),
    OTHER("Autre", true);

    private final String libelle;
    private final boolean scoreBased;

    Sport(String libelle, boolean scoreBased) {
        this.libelle = libelle;
        this.scoreBased = scoreBased;
    }

    public String getLibelle() {
        return libelle;
    }

    /** {@code true} si le sport se joue sur un score cumulatif (football, basket...). */
    public boolean isScoreBased() {
        return scoreBased;
    }
}
