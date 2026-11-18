package com.example.tournament.repository;

import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Accès aux données des tournois. */
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long>, JpaSpecificationExecutor<Tournament> {

    Optional<Tournament> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Tournament> findAllByOrderByStartDateDesc();

    List<Tournament> findByStatus(TournamentStatus status);

    long countByStatus(TournamentStatus status);

    long countByType(TournamentType type);

    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);

    @Query("select t from Tournament t where t.startDate <= :today and t.endDate >= :today")
    List<Tournament> findCurrentlyRunning(@Param("today") java.time.LocalDate today);
}
