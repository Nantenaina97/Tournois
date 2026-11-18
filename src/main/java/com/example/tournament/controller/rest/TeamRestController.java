package com.example.tournament.controller.rest;

import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.response.PageResponse;
import com.example.tournament.dto.response.TeamHistoryResponse;
import com.example.tournament.dto.response.TeamResponse;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.mapper.EntityMapper;
import com.example.tournament.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** API REST des équipes. */
@RestController
@RequestMapping("/api/teams")
public class TeamRestController {

    private final TeamService teamService;
    private final EntityMapper entityMapper;

    public TeamRestController(TeamService teamService, EntityMapper entityMapper) {
        this.teamService = teamService;
        this.entityMapper = entityMapper;
    }

    @GetMapping
    public ResponseEntity<PageResponse<TeamResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TeamStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), TournamentRestController.parseSort(sort));
        Page<Team> result = teamService.search(search, status, city, pageable);
        return ResponseEntity.ok(PageResponse.from(result, team -> entityMapper.toTeamResponse(team, false)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> findById(@PathVariable Long id) {
        Team team = teamService.findById(id)
                .orElseThrow(() -> com.example.tournament.exception.ResourceNotFoundException.of("Équipe", id));
        return ResponseEntity.ok(entityMapper.toTeamResponse(team, true));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        Team created = teamService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(entityMapper.toTeamResponse(created, true));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<TeamResponse> update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        Team updated = teamService.update(id, request);
        return ResponseEntity.ok(entityMapper.toTeamResponse(updated, true));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TeamHistoryResponse>> history(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getHistory(id));
    }
}
