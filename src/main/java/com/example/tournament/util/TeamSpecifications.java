package com.example.tournament.util;

import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Critères de filtrage/tri/recherche des équipes. */
public final class TeamSpecifications {

    private TeamSpecifications() {
    }

    public static Specification<Team> search(String term) {
        return (root, query, cb) -> {
            if (DateTimeUtils.isBlank(term)) {
                return cb.conjunction();
            }
            String like = "%" + term.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("name")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("city"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("coach"), "")), like));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Team> hasStatus(TeamStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Team> hasCity(String city) {
        return (root, query, cb) -> DateTimeUtils.isBlank(city) ? cb.conjunction()
                : cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase());
    }

    @SafeVarargs
    public static Specification<Team> all(Specification<Team>... specifications) {
        Specification<Team> result = null;
        for (Specification<Team> specification : specifications) {
            if (specification == null) {
                continue;
            }
            result = (result == null) ? specification : result.and(specification);
        }
        return result != null ? result : (root, query, cb) -> cb.conjunction();
    }
}
