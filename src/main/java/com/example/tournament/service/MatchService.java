package com.example.tournament.service;

import com.example.tournament.dto.request.GenerateMatchesRequest;
import com.example.tournament.dto.request.MatchResultRequest;
import com.example.tournament.dto.response.MatchGenerationResponse;
import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Gestion des matchs : génération, saisie des résultats, conflits d'horaire. */
public interface MatchService {

    @Transactional(readOnly = true)
    Optional<Match> findById(Long id);

    @Transactional(readOnly = true)
    Page<Match> search(Long tournamentId, MatchStatus status, MatchPhase phase, Integer roundNumber,
                       LocalDate from, LocalDate to, Long teamId, Pageable pageable);

    @Transactional(readOnly = true)
    List<Match> findByTournament(Long tournamentId);

    @Transactional(readOnly = true)
    List<Match> findByTournamentAndPhase(Long tournamentId, MatchPhase phase);

    /**
     * Génération automatique des rencontres du tournoi.
     * L'opération est atomique : contrôle des règles métier, création des matchs et
     * passage du statut du tournoi à PROGRAMME.
     */
    @Transactional
    MatchGenerationResponse generateMatches(Long tournamentId, GenerateMatchesRequest request);

    @Transactional
    Match saveResult(Long matchId, MatchResultRequest request);

    @Transactional
    Match updateMatch(Long matchId, MatchResultRequest request);

    @Transactional
    void deleteMatch(Long matchId);

    @Transactional
    Match changeStatus(Long matchId, MatchStatus status);

    @Transactional(readOnly = true)
    List<Match> upcoming(int limit);

    @Transactional(readOnly = true)
    long countFinished(Long tournamentId);
}
