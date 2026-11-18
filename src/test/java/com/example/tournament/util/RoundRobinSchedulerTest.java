package com.example.tournament.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Génération d'un championnat (round robin)")
class RoundRobinSchedulerTest {

    @ParameterizedTest(name = "{0} équipes")
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8, 9, 12, 16})
    @DisplayName("produit toutes les affiches possibles, une seule fois chacune")
    void singleRoundRobinGeneratesEveryPairOnce(int size) {
        List<List<Pair<Integer, Integer>>> rounds = RoundRobinScheduler.generateSingle(size);

        assertThat(rounds).hasSize(RoundRobinScheduler.numberOfRounds(size));

        Set<String> played = new HashSet<>();
        for (List<Pair<Integer, Integer>> round : rounds) {
            for (Pair<Integer, Integer> match : round) {
                played.add(key(match.first(), match.second()));
            }
        }

        int expected = size * (size - 1) / 2;
        assertThat(played).hasSize(expected);
        assertThat(rounds).allSatisfy(round -> assertThat(round)
                .hasSize(RoundRobinScheduler.matchesPerRound(size)));
    }

    @ParameterizedTest(name = "{0} équipes")
    @ValueSource(ints = {4, 5, 6, 7, 8})
    @DisplayName("ne fait jamais jouer une équipe deux fois dans la même journée")
    void noTeamPlaysTwiceInTheSameRound(int size) {
        List<List<Pair<Integer, Integer>>> rounds = RoundRobinScheduler.generateSingle(size);

        for (List<Pair<Integer, Integer>> round : rounds) {
            Set<Integer> teams = new HashSet<>();
            for (Pair<Integer, Integer> match : round) {
                assertThat(teams.add(match.first())).as("équipe %d déjà presente", match.first()).isTrue();
                assertThat(teams.add(match.second())).as("équipe %d déjà presente", match.second()).isTrue();
            }
        }
    }

    @Test
    @DisplayName("le championnat aller-retour rejoue chaque affiche avec les terrains inversés")
    void doubleRoundRobinInvertsHomeAndAway() {
        int size = 6;
        List<List<Pair<Integer, Integer>>> firstLeg = RoundRobinScheduler.generateSingle(size);
        List<List<Pair<Integer, Integer>>> bothLegs = RoundRobinScheduler.generateDouble(size);

        assertThat(bothLegs).hasSize(firstLeg.size() * 2);

        for (int i = 0; i < firstLeg.size(); i++) {
            List<Pair<Integer, Integer>> returnLeg = bothLegs.get(firstLeg.size() + i);
            assertThat(returnLeg).hasSameSizeAs(firstLeg.get(i));
            for (int j = 0; j < returnLeg.size(); j++) {
                Pair<Integer, Integer> forward = firstLeg.get(i).get(j);
                Pair<Integer, Integer> reverse = returnLeg.get(j);
                assertThat(reverse.first()).isEqualTo(forward.second());
                assertThat(reverse.second()).isEqualTo(forward.first());
            }
        }
    }

    @Test
    @DisplayName("l equilibrage domicile/terrain extérieur alterne")
    void homeAndAwayAreBalanced() {
        int size = 8;
        List<List<Pair<Integer, Integer>>> rounds = RoundRobinScheduler.generateSingle(size);

        int homeCount = 0;
        int total = 0;
        for (List<Pair<Integer, Integer>> round : rounds) {
            for (Pair<Integer, Integer> match : round) {
                total++;
                if (match.first() < match.second()) {
                    homeCount++;
                }
            }
        }

        assertThat(homeCount).isBetween(total / 4, 3 * total / 4);
    }

    @Test
    @DisplayName("renvoie une liste vide en dessous de deux équipes")
    void tooFewTeams() {
        assertThat(RoundRobinScheduler.generateSingle(1)).isEmpty();
        assertThat(RoundRobinScheduler.numberOfRounds(1)).isZero();
        assertThat(RoundRobinScheduler.matchesPerRound(0)).isZero();
    }

    private String key(int a, int b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }
}
