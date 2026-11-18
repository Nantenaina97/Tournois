package com.example.tournament.service;

import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.dto.response.TeamHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Gestion des équipes. */
public interface TeamService {

    Page<Team> search(String search, TeamStatus status, String city, Pageable pageable);

    List<Team> findAll();

    Optional<Team> findById(Long id);

    @Transactional
    Team create(TeamRequest request);

    @Transactional
    Team update(Long id, TeamRequest request);

    @Transactional
    void delete(Long id);

    @Transactional(readOnly = true)
    List<TeamHistoryResponse> getHistory(Long teamId);

    @Transactional(readOnly = true)
    List<Team> findByIds(List<Long> ids);

    @Transactional(readOnly = true)
    long count();
}
