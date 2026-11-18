package com.example.tournament.controller.rest;

import com.example.tournament.dto.request.GenerateMatchesRequest;
import com.example.tournament.dto.request.RegisterTeamRequest;
import com.example.tournament.dto.request.StatusChangeRequest;
import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.request.TournamentUpdateRequest;
import com.example.tournament.dto.response.BracketResponse;
import com.example.tournament.dto.response.MatchGenerationResponse;
import com.example.tournament.dto.response.MatchResponse;
import com.example.tournament.dto.response.PageResponse;
import com.example.tournament.dto.response.RegistrationResponse;
import com.example.tournament.dto.response.StandingResponse;
import com.example.tournament.dto.response.TournamentResponse;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.mapper.EntityMapper;
import com.example.tournament.mapper.TournamentMapper;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.RankingService;
import com.example.tournament.service.StatisticsService;
import com.example.tournament.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * API REST des tournois.
 *
 * <p>Expose uniquement des DTO (jamais les entités JPA) et renvoie les codes HTTP
 * appropriés : 201 à la création, 404 si la ressource n'existe pas, 409 en cas de
 * conflit, 422 pour une opération métier interdite.</p>
 */
@RestController
@RequestMapping("/api/tournaments")
public class TournamentRestController {

    private final TournamentService tournamentService;
    private final MatchService matchService;
    private final RankingService rankingService;
    private final StatisticsService statisticsService;
    private final TournamentMapper tournamentMapper;
    private final EntityMapper entityMapper;

    public TournamentRestController(TournamentService tournamentService,
                                    MatchService matchService,
                                    RankingService rankingService,
                                    StatisticsService statisticsService,
                                    TournamentMapper tournamentMapper,
                                    EntityMapper entityMapper) {
        this.tournamentService = tournamentService;
        this.matchService = matchService;
        this.rankingService = rankingService;
        this.statisticsService = statisticsService;
        this.tournamentMapper = tournamentMapper;
        this.entityMapper = entityMapper;
    }

    // ------------------------------------------------------------------ Tournois

    /** Liste paginée, triée, filtrable et recherchable. */
    @GetMapping
    public ResponseEntity<PageResponse<TournamentResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TournamentStatus status,
            @RequestParam(required = false) TournamentType type,
            @RequestParam(required = false) Sport sport,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate,desc") String sort) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), parseSort(sort));
        Page<Tournament> result = tournamentService.search(search, status, type, sport, from, to, pageable);
        return ResponseEntity.ok(PageResponse.from(result, t -> tournamentMapper.toResponse(t, true, false)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> findById(@PathVariable Long id) {
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> com.example.tournament.exception.ResourceNotFoundException.of("Tournoi", id));
        return ResponseEntity.ok(tournamentMapper.toResponse(tournament));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<TournamentResponse> create(@Valid @RequestBody TournamentRequest request) {
        Tournament created = tournamentService.create(request);
        TournamentResponse body = tournamentMapper.toResponse(created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<TournamentResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody TournamentUpdateRequest request) {
        Tournament updated = tournamentService.update(id, request);
        return ResponseEntity.ok(tournamentMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<TournamentResponse> changeStatus(@PathVariable Long id,
                                                           @Valid @RequestBody StatusChangeRequest request) {
        Tournament updated = tournamentService.changeStatus(id, request.status());
        return ResponseEntity.ok(tournamentMapper.toResponse(updated));
    }

    // -------------------------------------------------------------- Équipes

    @GetMapping("/{id}/teams")
    public ResponseEntity<List<RegistrationResponse>> teams(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getRegistrations(id).stream()
                .map(entityMapper::toRegistrationResponse)
                .toList());
    }

    @PostMapping("/{id}/teams")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<RegistrationResponse> registerTeam(@PathVariable Long id,
                                                             @Valid @RequestBody RegisterTeamRequest request) {
        var registration = tournamentService.registerTeam(id, request.teamId(), request.seedNumber());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{teamId}")
                .buildAndExpand(registration.getTeam().getId()).toUri();
        return ResponseEntity.created(location).body(entityMapper.toRegistrationResponse(registration));
    }

    @DeleteMapping("/{id}/teams/{teamId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> unregisterTeam(@PathVariable Long id, @PathVariable Long teamId) {
        tournamentService.unregisterTeam(id, teamId);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------- Calendrier & résultats

    @PostMapping("/{id}/generate-matches")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public ResponseEntity<MatchGenerationResponse> generateMatches(
            @PathVariable Long id,
            @RequestBody(required = false) GenerateMatchesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchService.generateMatches(id, request));
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<PageResponse<MatchResponse>> matches(
            @PathVariable Long id,
            @RequestParam(required = false) com.example.tournament.entity.MatchStatus status,
            @RequestParam(required = false) com.example.tournament.entity.MatchPhase phase,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "roundNumber,asc") String sort) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200), parseSort(sort));
        Page<com.example.tournament.entity.Match> result =
                matchService.search(id, status, phase, null, null, null, null, pageable);
        return ResponseEntity.ok(PageResponse.from(result, entityMapper::toMatchResponse));
    }

    @GetMapping("/{id}/ranking")
    public ResponseEntity<List<StandingResponse>> ranking(@PathVariable Long id) {
        return ResponseEntity.ok(entityMapper.toStandingResponses(rankingService.getStandings(id)));
    }

    @GetMapping("/{id}/bracket")
    public ResponseEntity<BracketResponse> bracket(@PathVariable Long id) {
        return ResponseEntity.ok(rankingService.buildBracket(id));
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<com.example.tournament.dto.response.TournamentStatisticsResponse> statistics(@PathVariable Long id) {
        return ResponseEntity.ok(statisticsService.tournamentStatistics(id));
    }

    // ------------------------------------------------------------------ Utilitaire

    static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "startDate");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, parts[0].trim());
    }
}
