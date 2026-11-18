package com.example.tournament.util;

import com.example.tournament.entity.Match;
import com.example.tournament.entity.MatchStatus;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Critères de filtrage/tri/recherche des tournois.
 * Utilisés par l'API REST et par l'interface MVC.
 */
public final class TournamentSpecifications {

    private TournamentSpecifications() {
    }

    public static Specification<Tournament> search(String term) {
        return (root, query, cb) -> {
            if (DateTimeUtils.isBlank(term)) {
                return cb.conjunction();
            }
            String like = "%" + term.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("name")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("venue"), "")), like));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Tournament> hasStatus(TournamentStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Tournament> hasStatusIn(List<TournamentStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Tournament> hasType(TournamentType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<Tournament> hasSport(com.example.tournament.entity.Sport sport) {
        return (root, query, cb) -> sport == null ? cb.conjunction() : cb.equal(root.get("sport"), sport);
    }

    public static Specification<Tournament> startsAfter(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("startDate"), date);
    }

    public static Specification<Tournament> startsBefore(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("startDate"), date);
    }

    public static Specification<Tournament> runningOn(LocalDate date) {
        return (root, query, cb) -> date == null ? cb.conjunction()
                : cb.and(cb.lessThanOrEqualTo(root.get("startDate"), date),
                         cb.greaterThanOrEqualTo(root.get("endDate"), date));
    }

    /** Combinaison de plusieurs spécifications (ET logique). */
    @SafeVarargs
    public static Specification<Tournament> all(Specification<Tournament>... specifications) {
        Specification<Tournament> result = null;
        for (Specification<Tournament> specification : specifications) {
            if (specification == null) {
                continue;
            }
            result = (result == null) ? specification : result.and(specification);
        }
        return result != null ? result : (root, query, cb) -> cb.conjunction();
    }
}
