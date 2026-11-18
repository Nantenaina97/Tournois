package com.example.tournament.controller.rest;

import com.example.tournament.dto.request.MatchResultRequest;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.dto.response.PageResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.mapper.EntityMapper;
import com.example.tournament.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** API REST des matchs. */
@RestController
@RequestMapping("/api/matches")
public class MatchRestController {

    private final MatchService matchService;
    private final EntityMapper entityMapper;

    public MatchRestController(MatchService matchService, EntityMapper entityMapper) {
        this.matchService = matchService;
        this.entityMapper = entityMapper;
    }

    @GetMapping
    public ResponseEntity<PageResponse<MatchResponse>> findAll(
            @RequestParam(required = false) Long tournamentId,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false) MatchPhase phase,
            @RequestParam(required = false) Integer round,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "matchDate,asc") String sort) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200), TournamentRestController.parseSort(sort));
        Page<Match> result = matchService.search(tournamentId, status, phase, round, from, to, teamId, pageable);
        return ResponseEntity.ok(PageResponse.from(result, entityMapper::toMatchResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> findById(@PathVariable Long id) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> com.example.tournament.exception.ResourceNotFoundException.of("Match", id));
        return ResponseEntity.ok(entityMapper.toMatchResponse(match));
    }

    /** Saisie (ou modification) du résultat : le classement est recalculé dans la même transaction. */
    @PutMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public ResponseEntity<MatchResponse> saveResult(@PathVariable Long id,
                                                    @Valid @RequestBody MatchResultRequest request) {
        return ResponseEntity.ok(entityMapper.toMatchResponse(matchService.saveResult(id, request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public ResponseEntity<MatchResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody MatchResultRequest request) {
        return ResponseEntity.ok(entityMapper.toMatchResponse(matchService.updateMatch(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public ResponseEntity<MatchResponse> changeStatus(@PathVariable Long id, @RequestParam MatchStatus status) {
        return ResponseEntity.ok(entityMapper.toMatchResponse(matchService.changeStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}
