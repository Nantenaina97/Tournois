package com.example.tournament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Point d'entrée de l'application « Système de gestion de tournois sportifs ».
 *
 * <p>L'application expose deux interfaces :
 * <ul>
 *     <li>une API REST sous {@code /api/**} (clients externes),</li>
 *     <li>une application MVC Thymeleaf (navigateur).</li>
 * </ul>
 * Les deux partagent exactement la même couche métier ({@code service}).</p>
 */
@SpringBootApplication
@EnableJpaAuditing
public class TournamentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TournamentManagementApplication.class, args);
    }
}
