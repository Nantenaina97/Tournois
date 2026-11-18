package com.example.tournament.repository;

import com.example.tournament.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Accès aux données des joueurs. */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByTeamIdOrderByJerseyNumberAsc(Long teamId);

    long countByTeamId(Long teamId);

    Optional<Player> findByTeamIdAndJerseyNumber(Long teamId, Integer jerseyNumber);
}
