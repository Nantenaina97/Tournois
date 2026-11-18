package com.example.tournament.repository;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Accès aux données des matchs. */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {

    List<Match> findByTournamentIdOrderByRoundNumberAscBracketPositionAsc(Long tournamentId);

    List<Match> findByTournamentIdAndRoundNumberOrderByBracketPositionAsc(Long tournamentId, Integer roundNumber);

    List<Match> findByTournamentIdAndStatus(Long tournamentId, MatchStatus status);

    List<Match> findByTournamentIdAndPhase(Long tournamentId, MatchPhase phase);

    Page<Match> findByTournamentId(Long tournamentId, Pageable pageable);

    long countByTournamentId(Long tournamentId);

    long countByTournamentIdAndStatus(Long tournamentId, MatchStatus status);

    boolean existsByTournamentId(Long tournamentId);

    Optional<Match> findByTournamentIdAndHomeTeamIdAndAwayTeamIdAndRoundNumber(
            Long tournamentId, Long homeTeamId, Long awayTeamId, Integer roundNumber);

    Optional<Match> findByTournamentIdAndRoundNumberAndBracketPosition(
            Long tournamentId, Integer roundNumber, Integer bracketPosition);

    boolean existsByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);

    @Query("select m from Match m where m.matchDate >= :today and m.status = 'PLANIFIE' "
            + "and m.homeTeam is not null and m.awayTeam is not null "
            + "order by m.matchDate asc, m.matchTime asc")
    List<Match> findUpcoming(@Param("today") LocalDate today, Pageable pageable);

    @Query("select m from Match m where m.matchDate = :date and m.status <> 'ANNULE'")
    List<Match> findOnDate(@Param("date") LocalDate date);

    @Query("select m from Match m where m.tournament.id = :tournamentId and m.bye = false")
    List<Match> findRealMatches(@Param("tournamentId") Long tournamentId);

    @Query("select m from Match m where (m.homeTeam.id in :teamIds or m.awayTeam.id in :teamIds) "
            + "and m.tournament.id = :tournamentId and m.status <> 'ANNULE'")
    List<Match> findMatchesOfTeams(@Param("teamIds") List<Long> teamIds,
                                   @Param("tournamentId") Long tournamentId);

    @Query("select m from Match m where m.matchDate between :from and :to and m.status = 'TERMINE'")
    List<Match> findFinishedBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select count(m) from Match m where m.tournament.id = :tournamentId and m.status = 'TERMINE' and m.bye = false")
    long countFinished(@Param("tournamentId") Long tournamentId);

    @Query("select m from Match m where m.tournament.id = :tournamentId and m.status <> 'TERMINE' and m.bye = false "
            + "and m.matchDate < :today")
    List<Match> findOverdue(@Param("tournamentId") Long tournamentId, @Param("today") LocalDate today);
}
