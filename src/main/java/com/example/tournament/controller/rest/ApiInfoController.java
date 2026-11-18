package com.example.tournament.controller.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Point d'entrée public de l'API (santé et informations). */
@RestController
@RequestMapping("/api")
public class ApiInfoController {

    @Value("${spring.application.name:tournament-management}")
    private String applicationName;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("application", applicationName);
        body.put("timestamp", Instant.now());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("application", applicationName);
        body.put("version", "1.0.0");
        body.put("description", "Système de gestion de tournois sportifs");
        body.put("authentication", "HTTP Basic");
        return ResponseEntity.ok(body);
    }
}
