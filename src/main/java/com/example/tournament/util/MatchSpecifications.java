package com.example.tournament.util;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.MatchStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Critères de filtrage/tri/recherche des matchs. */
public final class MatchSpecifications {

    private MatchSpecifications() {
    }

    public static Specification<Match> tournamentId(Long tournamentId) {
        return (root, query, cb) -> tournamentId == null ? cb.conjunction()
                : cb.equal(root.get("tournament").get("id"), tournamentId);
    }

    public static Specification<Match> hasStatus(MatchStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Match> hasPhase(MatchPhase phase) {
        return (root, query, cb) -> phase == null ? cb.conjunction() : cb.equal(root.get("phase"), phase);
    }

    public static Specification<Match> hasRound(Integer roundNumber) {
        return (root, query, cb) -> roundNumber == null ? cb.conjunction()
                : cb.equal(root.get("roundNumber"), roundNumber);
    }

    public static Specification<Match> betweenDates(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("matchDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("matchDate"), to));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Match> involvesTeam(Long teamId) {
        return (root, query, cb) -> {
            if (teamId == null) {
                return cb.conjunction();
            }
            return cb.or(cb.equal(root.get("homeTeam").get("id"), teamId),
                    cb.equal(root.get("awayTeam").get("id"), teamId));
        };
    }

    public static Specification<Match> playedOnly(boolean played) {
        return (root, query, cb) -> played ? cb.equal(root.get("status"), MatchStatus.TERMINE) : cb.conjunction();
    }

    @SafeVarargs
    public static Specification<Match> all(Specification<Match>... specifications) {
        Specification<Match> result = null;
        for (Specification<Match> specification : specifications) {
            if (specification == null) {
                continue;
            }
            result = (result == null) ? specification : result.and(specification);
        }
        return result != null ? result : (root, query, cb) -> cb.conjunction();
    }
}
