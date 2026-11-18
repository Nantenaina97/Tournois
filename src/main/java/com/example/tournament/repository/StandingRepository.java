package com.example.tournament.repository;

import com.example.tournament.entity.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Accès aux lignes de classement. */
@Repository
public interface StandingRepository extends JpaRepository<Standing, Long> {

    List<Standing> findByTournamentIdOrderByRankAsc(Long tournamentId);

    List<Standing> findByTournamentId(Long tournamentId);

    Optional<Standing> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    void deleteByTournamentId(Long tournamentId);

    long countByTournamentId(Long tournamentId);
}
