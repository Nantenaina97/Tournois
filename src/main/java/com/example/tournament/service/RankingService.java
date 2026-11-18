package com.example.tournament.service;

import com.example.tournament.dto.response.BracketResponse;
import com.example.tournament.entity.Standing;
import com.example.tournament.entity.Tournament;

import java.util.List;

/** Calcul du classement et représentation du tableau d'élimination directe. */
public interface RankingService {

    /**
     * Recalcule intégralement le classement d'un championnat à partir des résultats
     * enregistrés. Appelé dans la même transaction que la saisie du résultat.
     */
    List<Standing> recompute(Tournament tournament);

    List<Standing> getStandings(Long tournamentId);

    List<Standing> getTop(Long tournamentId, int limit);

    BracketResponse buildBracket(Long tournamentId);
}
