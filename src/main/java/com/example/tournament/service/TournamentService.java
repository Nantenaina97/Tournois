package com.example.tournament.service;

import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.request.TournamentUpdateRequest;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.TeamRegistration;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Gestion des tournois (CRUD, statuts, inscriptions). */
public interface TournamentService {

    Page<Tournament> search(String search, TournamentStatus status, TournamentType type, Sport sport,
                            java.time.LocalDate from, java.time.LocalDate to, Pageable pageable);

    Optional<Tournament> findById(Long id);

    List<Tournament> findAll();

    @Transactional
    Tournament create(TournamentRequest request);

    @Transactional
    Tournament update(Long id, TournamentUpdateRequest request);

    @Transactional
    void delete(Long id);

    @Transactional
    Tournament changeStatus(Long id, TournamentStatus newStatus);

    @Transactional
    TeamRegistration registerTeam(Long tournamentId, Long teamId, Integer seedNumber);

    @Transactional
    void unregisterTeam(Long tournamentId, Long teamId);

    @Transactional(readOnly = true)
    List<TeamRegistration> getRegistrations(Long tournamentId);

    @Transactional(readOnly = true)
    long countByStatus(TournamentStatus status);
}
