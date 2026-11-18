package com.example.tournament.entity;

/**
 * Critères de classement, hiérarchisés selon leur ordre dans la liste du tournoi.
 * Le premier critère est le plus prioritaire.
 */
public enum RankingCriteria {

    POINTS("Points"),
    GOAL_DIFFERENCE("Différence de buts"),
    GOALS_SCORED("Buts marqués"),
    GOALS_CONCEDED("Buts encaissés"),
    WINS("Nombre de victoires"),
    HEAD_TO_HEAD("Confrontation directe");

    private final String libelle;

    RankingCriteria(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
