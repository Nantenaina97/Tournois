package com.example.tournament.util;

import com.example.tournament.entity.MatchPhase;
import com.example.tournament.entity.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Moteur de génération d'un tableau d'élimination directe.
 *
 * <p>Le nombre de tours est déterminé automatiquement : la taille du tableau est la plus
 * grande puissance de deux inférieure ou égale au nombre de participants. Les équipes sont
 * ensuite réparties selon l'ordre de tête de série canonique, ce qui garantit que les deux
 * meilleures ne peuvent se rencontrer qu'en finale.</p>
 *
 * <p>Lorsque le nombre d'équipes n'est pas une puissance de deux, un
 * <strong>tour préliminaire</strong> est créé : les {@code n - tailleTableau} équipes les
 * moins bien classées s'affrontent entre elles, tandis que les autres bénéficient d'un
 * <strong>exempt</strong> (bye) et rejoignent directement le tableau principal.</p>
 *
 * <p>Exemple pour 10 équipes : taille du tableau = 8, tour préliminaire de 2 matchs
 * (équipes 7 à 10), 6 exempts, puis huitièmes, quarts, demi-finales et finale.</p>
 */
public final class BracketGenerator {

    private BracketGenerator() {
    }

    // ------------------------------------------------------------------- Records

    /**
     * Un emplacement du tableau.
     *
     * @param team     équipe connue à cet emplacement (1er tour uniquement)
     * @param label    libellé lisible (« Vainqueur du quart de finale n°1 »)
     * @param resolved {@code true} si l'équipe est identifiée
     */
    public record Slot(Team team, String label, boolean resolved) {

        public static Slot of(Team team) {
            return new Slot(team, team != null ? team.getName() : null, true);
        }

        public static Slot ofLabel(String label) {
            return new Slot(null, label, false);
        }

        public static Slot empty() {
            return new Slot(null, null, false);
        }
    }

    /** Une affranchise planifiée. */
    public record PlannedMatch(int roundNumber, MatchPhase phase, String label,
                               int position, Slot first, Slot second) {

        public boolean bothResolved() {
            return first.resolved() && second.resolved();
        }
    }

    /** Un tour du tableau. */
    public record RoundPlan(int roundNumber, MatchPhase phase, String label, int order,
                            List<PlannedMatch> matches) {
    }

    /** Le plan complet d'un tableau d'élimination directe. */
    public record Plan(int numberOfTeams, int bracketSize, int preliminaryMatches, int byes,
                       List<RoundPlan> rounds) {

        public int totalMatches() {
            return rounds.stream().mapToInt(r -> r.matches().size()).sum();
        }

        public int numberOfKnockoutRounds() {
            return Math.max(rounds.size() - (preliminaryMatches > 0 ? 1 : 0), 0);
        }
    }

    // ------------------------------------------------------------------ Méthodes

    /**
     * @return la plus grande puissance de deux inférieure ou égale à {@code numberOfTeams}
     */
    public static int bracketSize(int numberOfTeams) {
        if (numberOfTeams < 2) {
            return 0;
        }
        int size = 2;
        while (size * 2 <= numberOfTeams) {
            size *= 2;
        }
        return size;
    }

    /** Nombre de tours au-delà du tour préliminaire (log2 de la taille du tableau). */
    public static int numberOfKnockoutRounds(int numberOfTeams) {
        int size = bracketSize(numberOfTeams);
        int rounds = 0;
        while (size > 1) {
            rounds++;
            size /= 2;
        }
        return rounds;
    }

    /**
     * Construit le plan du tableau.
     *
     * @param teamsBySeed équipes triées par tête de série croissante (la 1re est la mieux classée)
     * @throws IllegalArgumentException si moins de deux équipes
     */
    public static Plan build(List<Team> teamsBySeed) {
        int n = teamsBySeed.size();
        if (n < 2) {
            throw new IllegalArgumentException("Un tableau d'élimination directe requiert au moins 2 équipes.");
        }

        int size = bracketSize(n);
        int preliminary = n - size;          // nombre de matchs de préliminaire
        int byeCount = size - preliminary;    // nombre d'équipes exemptes (= têtes de série 1..byeCount)

        List<RoundPlan> rounds = new ArrayList<>();

        // ---- Tour préliminaire ----------------------------------------------------
        if (preliminary > 0) {
            List<PlannedMatch> prelimMatches = new ArrayList<>(preliminary);
            for (int i = 0; i < preliminary; i++) {
                Team home = teamsBySeed.get(byeCount + i * 2);
                Team away = teamsBySeed.get(byeCount + i * 2 + 1);
                prelimMatches.add(new PlannedMatch(1, MatchPhase.TOUR_PRELIMINAIRE,
                        MatchPhase.TOUR_PRELIMINAIRE.getLibelle(), i, Slot.of(home), Slot.of(away)));
            }
            rounds.add(new RoundPlan(1, MatchPhase.TOUR_PRELIMINAIRE,
                    MatchPhase.TOUR_PRELIMINAIRE.getLibelle(), 0, prelimMatches));
        }

        // ---- Placement dans l'ordre canonique de tête de série --------------------
        int[] order = seedOrder(size);
        List<Slot> orderedSlots = new ArrayList<>(size);
        for (int position = 0; position < size; position++) {
            int seed = order[position];
            if (seed <= byeCount) {
                orderedSlots.add(Slot.of(teamsBySeed.get(seed - 1)));
            } else {
                // Vainqueur du tour préliminaire n° (seed - byeCount)
                orderedSlots.add(Slot.ofLabel(preliminaryWinnerLabel(seed - byeCount)));
            }
        }

        // ---- Tours du tableau principal -------------------------------------------
        int currentSize = size;
        int roundNumber = preliminary > 0 ? 2 : 1;
        while (currentSize >= 2) {
            MatchPhase phase = phaseFor(currentSize);
            String label = phase.getLibelle();
            List<PlannedMatch> matches = new ArrayList<>(currentSize / 2);
            for (int i = 0; i < currentSize / 2; i++) {
                matches.add(new PlannedMatch(roundNumber, phase, label, i,
                        orderedSlots.get(i * 2), orderedSlots.get(i * 2 + 1)));
            }
            rounds.add(new RoundPlan(roundNumber, phase, label, rounds.size(), matches));

            if (currentSize > 2) {
                List<Slot> next = new ArrayList<>(currentSize / 2);
                for (int i = 0; i < currentSize / 2; i++) {
                    next.add(Slot.ofLabel(winnerLabel(label, i)));
                }
                orderedSlots = next;
            }
            currentSize /= 2;
            roundNumber++;
        }

        return new Plan(n, size, preliminary, byeCount, rounds);
    }

    /** Phase correspondant à la taille courante du tableau. */
    public static MatchPhase phaseFor(int currentSize) {
        return switch (currentSize) {
            case 16 -> MatchPhase.SEIZIEME_FINALE;
            case 8 -> MatchPhase.QUART_FINALE;
            case 4 -> MatchPhase.DEMI_FINALE;
            case 2 -> MatchPhase.FINALE;
            default -> MatchPhase.FINALE;
        };
    }

    /**
     * Ordre de tête de série canonique (« snake draft ») : 1 vs n, puis 2 vs n-1, etc.,
     * en alternant de l'extérieur vers l'intérieur du tableau.
     *
     * <p>Pour 8 équipes : 1-8, 6-4, 3-5, 7-2. Les deux meilleures têtes de série occupent
     * deux moitiés opposées du tableau et ne peuvent se rencontrer qu'en finale.</p>
     */
    public static int[] seedOrder(int size) {
        if (size < 2) {
            return new int[Math.max(size, 0)];
        }
        int[] order = new int[size];
        order[0] = 1;
        order[size - 1] = 2;

        int left = size / 2;
        int right = size / 2 - 1;
        boolean toLeft = true;
        for (int seed = 3; seed <= size; seed++) {
            if (toLeft) {
                order[left++] = seed;
            } else {
                order[right--] = seed;
            }
            toLeft = !toLeft;
        }
        return order;
    }

    /** Position du match du tour suivant alimenté par le match {@code position}. */
    public static int nextPosition(int position) {
        return position / 2;
    }

    public static String preliminaryWinnerLabel(int index) {
        return "Vainqueur du tour préliminaire " + index;
    }

    public static String winnerLabel(String roundLabel, int position) {
        return "Vainqueur " + roundLabel.toLowerCase() + " " + (position + 1);
    }
}
