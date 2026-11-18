package com.example.tournament.repository;

import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Accès aux données des équipes. */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    Optional<Team> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Team> findAllByOrderByNameAsc();

    List<Team> findByStatus(TeamStatus status);

    long countByStatus(TeamStatus status);

    Page<Team> findByStatus(TeamStatus status, Pageable pageable);

    @Query("select t from Team t where lower(t.name) like lower(concat('%', :term, '%')) "
            + "or lower(t.city) like lower(concat('%', :term, '%')) "
            + "or lower(t.coach) like lower(concat('%', :term, '%'))")
    List<Team> search(@org.springframework.data.repository.query.Param("term") String term);
}
