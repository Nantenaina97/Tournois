package com.example.tournament.repository;

import com.example.tournament.entity.TeamRegistration;
import com.example.tournament.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Accès aux inscriptions d'équipes aux tournois. */
@Repository
public interface TeamRegistrationRepository extends JpaRepository<TeamRegistration, Long> {

    Optional<TeamRegistration> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    long countByTournamentIdAndStatus(Long tournamentId, RegistrationStatus status);

    List<TeamRegistration> findByTournamentId(Long tournamentId);

    List<TeamRegistration> findByTournamentIdOrderByIdAsc(Long tournamentId);

    List<TeamRegistration> findByTournamentIdAndStatusOrderBySeedNumberAsc(Long tournamentId, RegistrationStatus status);

    @Query("select r from TeamRegistration r where r.team.id = :teamId order by r.registrationDate desc")
    List<TeamRegistration> findHistoryOfTeam(@Param("teamId") Long teamId);

    @Query("select r from TeamRegistration r where r.tournament.id in :ids")
    List<TeamRegistration> findByTournamentIds(@Param("ids") List<Long> ids);
}
