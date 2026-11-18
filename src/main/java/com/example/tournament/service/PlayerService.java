package com.example.tournament.service;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.entity.Player;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestion des joueurs. */
public interface PlayerService {

    @Transactional(readOnly = true)
    List<Player> findByTeam(Long teamId);

    @Transactional(readOnly = true)
    long countByTeam(Long teamId);

    @Transactional
    Player addPlayer(Long teamId, PlayerRequest request);

    @Transactional
    Player updatePlayer(Long teamId, Long playerId, PlayerRequest request);

    @Transactional
    void removePlayer(Long teamId, Long playerId);
}
