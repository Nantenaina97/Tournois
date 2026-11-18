package com.example.tournament.service.impl;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.entity.Player;
import com.example.tournament.entity.Team;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.RequestMapper;
import com.example.tournament.repository.PlayerRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.service.PlayerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Implémentation de la gestion des joueurs. */
@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final RequestMapper requestMapper;

    public PlayerServiceImpl(PlayerRepository playerRepository, TeamRepository teamRepository,
                             RequestMapper requestMapper) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.requestMapper = requestMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> findByTeam(Long teamId) {
        return playerRepository.findByTeamIdOrderByJerseyNumberAsc(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTeam(Long teamId) {
        return playerRepository.countByTeamId(teamId);
    }

    @Override
    @Transactional
    public Player addPlayer(Long teamId, PlayerRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> ResourceNotFoundException.of("Équipe", teamId));
        if (request.jerseyNumber() != null) {
            Optional<Player> existing = playerRepository.findByTeamIdAndJerseyNumber(teamId, request.jerseyNumber());
            if (existing.isPresent()) {
                throw new ConflictException("Le numéro " + request.jerseyNumber()
                        + " est déjà attribué à un joueur de l'équipe " + team.getName() + ".");
            }
        }
        Player player = requestMapper.toEntity(request);
        player.setTeam(team);
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public Player updatePlayer(Long teamId, Long playerId, PlayerRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> ResourceNotFoundException.of("Équipe", teamId));
        Player player = playerRepository.findById(playerId)
                .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(teamId))
                .orElseThrow(() -> ResourceNotFoundException.of("Joueur", playerId));
        if (request.jerseyNumber() != null && !request.jerseyNumber().equals(player.getJerseyNumber())) {
            playerRepository.findByTeamIdAndJerseyNumber(teamId, request.jerseyNumber())
                    .ifPresent(other -> {
                        throw new ConflictException("Le numéro " + request.jerseyNumber() + " est déjà utilisé.");
                    });
        }
        requestMapper.applyPlayer(request, player);
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public void removePlayer(Long teamId, Long playerId) {
        Player player = playerRepository.findById(playerId)
                .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(teamId))
                .orElseThrow(() -> ResourceNotFoundException.of("Joueur", playerId));
        playerRepository.delete(player);
    }
}
