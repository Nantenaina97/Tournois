package com.example.tournament.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Moteur de génération des rencontres d'un championnat (« round robin »).
 *
 * <p>Utilise la <em>méthode du cercle</em> : une équipe<code>0</code> est placée
 * sur une position fixe, les autres sont décalées à chaque tour puis le nombre de
 * positions est diminué de deux. Elle garantit qu'une équipe ne joue
 * qu'une fois par journée et que toutes les affiches sont générées
 * après {@code n-1} journées (ou {@code n} si le nombre d'équipes est impair).</p>
 */
public final class RoundRobinScheduler {

    private RoundRobinScheduler() {
    }

    /**
     * @param size nombre d'équipes
     * @return nombre de journées nécessaires
     */
    public static int numberOfRounds(int size) {
        if (size < 2) {
            return 0;
        }
        return size % 2 == 0 ? size - 1 : size;
    }

    /**
     * @param size nombre d'équipes
     * @return nombre de matchs par journée
     */
    public static int matchesPerRound(int size) {
        return size / 2;
    }

    /**
     * Génère le calendrier d'une saison simple (une seule rencontre par paire).
     *
     * @param size nombre d'équipes (indices 0..size-1)
     * @return liste de journées, chacune contenant des paires d'indices
     */
    public static List<List<Pair<Integer, Integer>>> generateSingle(int size) {
        List<List<Pair<Integer, Integer>>> rounds = new ArrayList<>();
        if (size < 2) {
            return rounds;
        }
        // Nombre pair minimal >= size, afin d'avoir une position « fantôme » si size est impair.
        int positions = size % 2 == 0 ? size : size + 1;
        List<Integer> rotation = new ArrayList<>();
        for (int i = 0; i < positions; i++) {
            rotation.add(i);
        }

        for (int round = 0; round < positions - 1; round++) {
            List<Pair<Integer, Integer>> matches = new ArrayList<>();
            for (int i = 0; i < positions / 2; i++) {
                int home = rotation.get(i);
                int away = rotation.get(positions - 1 - i);
                if (home >= size || away >= size) {
                    continue; // au moins l'un des deux est la position fantôme
                }
                // On alterne le sens du terrain pour équilibrer domicile / extérieur.
                boolean swap = (round % 2 == 1) ^ (home > away);
                matches.add(swap ? new Pair<>(away, home) : new Pair<>(home, away));
            }
            rounds.add(matches);
            // rotation : le premier élément reste fixe, les autres glissent d'un cran.
            if (round < positions - 2) {
                rotation.add(1, rotation.remove(positions - 1));
            }
        }
        return rounds;
    }

    /**
     * Génère un calendrier aller-retour : la seconde moitié de saison reprend les
     * mêmes affiches avec les rôles domicile/extérieur inversés.
     *
     * @param size nombre d'équipes
     * @return liste complète de journées
     */
    public static List<List<Pair<Integer, Integer>>> generateDouble(int size) {
        List<List<Pair<Integer, Integer>>> rounds = new ArrayList<>(generateSingle(size));
        int firstLeg = rounds.size();
        for (int i = 0; i < firstLeg; i++) {
            List<Pair<Integer, Integer>> returnLeg = new ArrayList<>();
            for (Pair<Integer, Integer> match : rounds.get(i)) {
                returnLeg.add(new Pair<>(match.second(), match.first()));
            }
            rounds.add(returnLeg);
        }
        return rounds;
    }
}
