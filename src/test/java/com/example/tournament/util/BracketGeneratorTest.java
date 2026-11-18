package com.example.tournament.util;

import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Génération d'un tableau d'élimination directe")
class BracketGeneratorTest {

    @ParameterizedTest(name = "{0} équipes -> tableau de {1}")
    @CsvSource({
            "2, 2",
            "3, 2",
            "4, 4",
            "5, 4",
            "6, 4",
            "7, 4",
            "8, 8",
            "9, 8",
            "10, 8",
            "12, 8",
            "16, 16"
    })
    @DisplayName("retient la plus grande puissance de deux inférieure au nombre de participants")
    void bracketSizeIsTheLargestLowerPowerOfTwo(int teams, int expectedSize) {
        assertThat(BracketGenerator.bracketSize(teams)).isEqualTo(expectedSize);
    }

    @Test
    @DisplayName("cinq équipes : un match préliminaire et trois exempts")
    void fiveTeamsProduceOnePreliminaryMatchAndThreeByes() {
        BracketGenerator.Plan plan = BracketGenerator.build(teams(5));

        assertThat(plan.bracketSize()).isEqualTo(4);
        assertThat(plan.preliminaryMatches()).isEqualTo(1);
        assertThat(plan.byes()).isEqualTo(3);

        List<BracketGenerator.RoundPlan> rounds = plan.rounds();
        assertThat(rounds.get(0).phase()).isEqualTo(MatchPhase.TOUR_PRELIMINAIRE);
        assertThat(rounds.get(0).matches()).hasSize(1);
    }

    @Test
    @DisplayName("dix équipes : deux matchs préliminaires et six exempts")
    void tenTeamsProduceTwoPreliminaryMatchesAndSixByes() {
        BracketGenerator.Plan plan = BracketGenerator.build(teams(10));

        assertThat(plan.bracketSize()).isEqualTo(8);
        assertThat(plan.preliminaryMatches()).isEqualTo(2);
        assertThat(plan.byes()).isEqualTo(6);
        assertThat(plan.rounds().get(0).matches()).hasSize(2);
    }

    @ParameterizedTest(name = "{0} équipes")
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 16, 17, 32})
    @DisplayName("chaque équipe apparaît une seule fois dans le tableau")
    void everyTeamAppearsExactlyOnceInThePlan(int size) {
        BracketGenerator.Plan plan = BracketGenerator.build(teams(size));

        List<Long> teamIds = plan.rounds().stream()
                .flatMap(round -> round.matches().stream())
                .flatMap(match -> List.of(match.first(), match.second()).stream())
                .filter(BracketGenerator.Slot::resolved)
                .map(slot -> slot.team().getId())
                .toList();

        assertThat(teamIds).hasSize(size);
        assertThat(Set.copyOf(teamIds)).hasSize(size);
    }

    @Test
    @DisplayName("les deux meilleures têtes de série sont placées de part et d'autre du tableau")
    void topSeedsCannotMeetBeforeTheFinal() {
        BracketGenerator.Plan plan = BracketGenerator.build(teams(8));

        List<BracketGenerator.PlannedMatch> firstRound = plan.rounds().get(0).matches();
        Set<Long> firstRoundTeams = firstRound.stream()
                .flatMap(m -> List.of(m.first(), m.second()).stream())
                .filter(BracketGenerator.Slot::resolved)
                .map(s -> s.team().getId())
                .collect(Collectors.toSet());

        assertThat(firstRoundTeams).contains(1L, 2L);
        boolean sameMatch = firstRound.stream()
                .anyMatch(m -> (m.first().resolved() && m.second().resolved())
                        && m.first().team().getId() == 1L && m.second().team().getId() == 2L);
        assertThat(sameMatch).isFalse();
    }

    @Test
    @DisplayName("le nombre total de rencontres correspond à la taille du tableau")
    void totalMatchesIsBracketSizeMinusOne() {
        for (int size : new int[]{4, 8, 16}) {
            BracketGenerator.Plan plan = BracketGenerator.build(teams(size));
            assertThat(plan.totalMatches()).isEqualTo(size - 1);
        }
    }

    @Test
    @DisplayName("le tableau se termine toujours par une finale unique")
    void lastRoundIsTheFinal() {
        BracketGenerator.Plan plan = BracketGenerator.build(teams(10));

        List<BracketGenerator.RoundPlan> rounds = plan.rounds();
        BracketGenerator.RoundPlan last = rounds.get(rounds.size() - 1);
        assertThat(last.phase()).isEqualTo(MatchPhase.FINALE);
        assertThat(last.matches()).hasSize(1);
    }

    @Test
    @DisplayName("refuse un tableau de moins de deux équipes")
    void tooFewTeams() {
        try {
            BracketGenerator.build(teams(1));
            org.junit.jupiter.api.Assertions.fail("Une exception était attendue");
        } catch (IllegalArgumentException ex) {
            assertThat(ex).hasMessageContaining("2 équipes");
        }
    }

    private List<Team> teams(int count) {
        List<Team> teams = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Team team = new Team();
            team.setId((long) i);
            team.setName("Équipe " + i);
            teams.add(team);
        }
        return teams;
    }
}
