package com.example.tournament.dto.response;

import com.example.tournament.entity.TeamStatus;

import java.time.Instant;
import java.util.List;

/** Représentation d'une équipe. */
public record TeamResponse(
        Long id,
        String name,
        String logoUrl,
        String city,
        String coach,
        String coachPhone,
        Integer foundedYear,
        TeamStatus status,
        String statusLabel,
        Long playerCount,
        Long tournamentCount,
        List<PlayerResponse> players) {
}
