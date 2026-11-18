package com.example.tournament.controller.rest;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.dto.response.PlayerResponse;
import com.example.tournament.entity.Player;
import com.example.tournament.mapper.EntityMapper;
import com.example.tournament.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** API REST des joueurs. */
@RestController
@RequestMapping("/api/teams/{teamId}/players")
public class PlayerRestController {

    private final PlayerService playerService;
    private final EntityMapper entityMapper;

    public PlayerRestController(PlayerService playerService, EntityMapper entityMapper) {
        this.playerService = playerService;
        this.entityMapper = entityMapper;
    }

    @GetMapping
    public ResponseEntity<List<PlayerResponse>> findAll(@PathVariable Long teamId) {
        return ResponseEntity.ok(entityMapper.toPlayerResponses(playerService.findByTeam(teamId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<PlayerResponse> create(@PathVariable Long teamId,
                                                 @Valid @RequestBody PlayerRequest request) {
        Player created = playerService.addPlayer(teamId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(entityMapper.toPlayerResponse(created));
    }

    @PutMapping("/{playerId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<PlayerResponse> update(@PathVariable Long teamId, @PathVariable Long playerId,
                                                 @Valid @RequestBody PlayerRequest request) {
        Player updated = playerService.updatePlayer(teamId, playerId, request);
        return ResponseEntity.ok(entityMapper.toPlayerResponse(updated));
    }

    @DeleteMapping("/{playerId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long teamId, @PathVariable Long playerId) {
        playerService.removePlayer(teamId, playerId);
        return ResponseEntity.noContent().build();
    }
}
