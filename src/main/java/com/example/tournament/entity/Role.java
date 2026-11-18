package com.example.tournament.entity;

/** Rôles applicatifs. Les droits diffèrent selon le rôle. */
public enum Role {

    /** Configuration : gestion des utilisateurs, des paramètres et suppression. */
    ADMIN("Administrateur"),
    /** Gestion métier : création et paramétrage des tournois. */
    MANAGER("Manager"),
    /** Opérations quotidiennes : saisie des résultats, suivi. */
    OPERATOR("Opérateur"),
    /** Consultation uniquement. */
    USER("Utilisateur");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
