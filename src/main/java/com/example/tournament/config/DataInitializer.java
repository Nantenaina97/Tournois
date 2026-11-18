package com.example.tournament.config;

import com.example.tournament.dto.request.MatchResultRequest;
import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.entity.AppUser;
import com.example.tournament.entity.RankingCriteria;
import com.example.tournament.entity.Role;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.repository.AppUserRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.service.MatchService;
import com.example.tournament.service.PlayerService;
import com.example.tournament.service.TeamService;
import com.example.tournament.service.TournamentService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

/**
 * Jeu de données de démonstration.
 * N'est exécuté que si la base est vide (H2 en mémoire au premier démarrage).
 */
@Configuration
public class DataInitializer implements SmartInitializingSingleton {

    private static final String[] PRENOMS = {"Lucas", "Hugo", "Enzo", "Nathan", "Théo", "Maxime", "Antoine", "Julien"};
    private static final String[] POSTES = {"Gardien", "Défenseur", "Milieu", "Attaquant", "Attaquant"};

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final MatchService matchService;

    public DataInitializer(AppUserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           TournamentService tournamentService,
                           TeamService teamService,
                           PlayerService playerService,
                           TournamentRepository tournamentRepository,
                           TeamRepository teamRepository,
                           MatchService matchService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tournamentService = tournamentService;
        this.teamService = teamService;
        this.playerService = playerService;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.matchService = matchService;
    }

    /**
     * Jeu de données de démonstration injecté avant l'ouverture du port HTTP.
     *
     * <p>{@code SmartInitializingSingleton} est exécuté pendant
     * {@code finishBeanFactoryInitialization}, donc avant {@code finishRefresh()} qui
     * démarre le serveur web. Avec un {@code ApplicationRunner}, le port était déjà ouvert
     * et les premières requêtes API échouaient en 401 car les comptes utilisateurs
     * n'existaient pas encore en base.</p>
     */
    @Override
    public void afterSingletonsInstantiated() {
        seedUsers(userRepository, passwordEncoder);
        if (tournamentRepository.count() > 0) {
            return;
        }
        List<Team> teams = seedTeams(teamRepository, teamService);
        seedPlayers(teams, playerService);
        seedTournaments(teams, tournamentService, matchService);
    }

    // ------------------------------------------------------------------ Utilisateurs

    private void seedUsers(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        createUser(userRepository, passwordEncoder, "admin", "admin123", "Administrateur Système",
                "admin@tournois.fr", Role.ADMIN);
        createUser(userRepository, passwordEncoder, "manager", "manager123", "Manager Sportif",
                "manager@tournois.fr", Role.MANAGER);
        createUser(userRepository, passwordEncoder, "operateur", "operateur123", "Opérateur Terrain",
                "operateur@tournois.fr", Role.OPERATOR);
        createUser(userRepository, passwordEncoder, "user", "user123", "Consultateur",
                "user@tournois.fr", Role.USER);
    }

    private void createUser(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
                            String username, String rawPassword, String fullName, String email, Role role) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        userRepository.save(user);
    }

    // ---------------------------------------------------------------------- Équipes

    private List<Team> seedTeams(TeamRepository teamRepository, TeamService teamService) {
        if (teamRepository.count() > 0) {
            return teamRepository.findAllByOrderByNameAsc();
        }
        String[][] clubs = {
                {"Olympique Lyonnais", "Lyon", "1970"},
                {"Paris Saint-Germain", "Paris", "1970"},
                {"AS Monaco", "Monaco", "1924"},
                {"LOSC Lille", "Lille", "1944"},
                {"Stade Rennais", "Rennes", "1901"},
                {"Marseille Olympique", "Marseille", "1899"},
                {"FC Nantes", "Nantes", "1943"},
                {"RC Strasbourg", "Strasbourg", "1906"}
        };
        for (String[] club : clubs) {
            TeamRequest request = new TeamRequest(
                    club[0],
                    "https://ui-avatars.com/api/?name=" + club[0].replace(" ", "+")
                            + "&background=0f3460&color=ffffff&size=200&bold=true",
                    club[1],
                    "Coach " + club[1],
                    "+33 1 02 03 04 05",
                    Integer.valueOf(club[2]),
                    null);
            teamService.create(request);
        }
        return teamRepository.findAllByOrderByNameAsc();
    }

    private void seedPlayers(List<Team> teams, PlayerService playerService) {
        for (int t = 0; t < teams.size(); t++) {
            Team team = teams.get(t);
            for (int p = 0; p < 5; p++) {
                playerService.addPlayer(team.getId(), new PlayerRequest(
                        PRENOMS[(t + p) % PRENOMS.length],
                        team.getCity().substring(0, 3).toUpperCase() + (p + 1),
                        POSTES[p],
                        p + 1,
                        LocalDate.of(1995 + (p % 8), 1 + (p * 2), 5),
                        "France"));
            }
        }
    }

    // -------------------------------------------------------------------- Tournois

    private void seedTournaments(List<Team> teams, TournamentService tournamentService, MatchService matchService) {
        // 1) Championnat aller-retour de 8 équipes, en cours (première journée jouée).
        Tournament league = tournamentService.create(new TournamentRequest(
                "Coupe de Ligue 2026", Sport.FOOTBALL,
                "Championnat aller-retour regroupant les huit clubs de la région.",
                LocalDate.now().minusDays(21), LocalDate.now().plusDays(28),
                "Stade Olympique de la Vallée", 8, TournamentType.CHAMPIONNAT_ALLER_RETOUR,
                3, 1,
                List.of(RankingCriteria.POINTS, RankingCriteria.GOAL_DIFFERENCE,
                        RankingCriteria.GOALS_SCORED, RankingCriteria.HEAD_TO_HEAD),
                7, 90, "15:00"));
        registerAll(teams, league.getId(), tournamentService, true);
        matchService.generateMatches(league.getId(), null);
        playFirstLeg(league.getId(), matchService);

        // 2) Championnat simple de 6 équipes, calendrier programmé.
        Tournament simple = tournamentService.create(new TournamentRequest(
                "Régional U19 — Poule A", Sport.FOOTBALL,
                "Championnat simple : chaque équipe rencontre les six autres une seule fois.",
                LocalDate.now().plusDays(7), null,
                "Complexe sportif du Val", 6, TournamentType.CHAMPIONNAT_SIMPLE,
                3, 1, List.of(RankingCriteria.POINTS, RankingCriteria.GOAL_DIFFERENCE), 7, 90, "14:00"));
        registerAll(teams.subList(0, 6), simple.getId(), tournamentService, true);
        matchService.generateMatches(simple.getId(), null);

        // 3) Élimination directe de 8 équipes, prête à être lancée depuis l'interface.
        Tournament cup = tournamentService.create(new TournamentRequest(
                "Coupe Nationale — Phase finale", Sport.BASKETBALL,
                "Élimination directe : huitièmes, quarts, demi-finales et finale.",
                LocalDate.now().plusDays(14), null,
                "Pal omnisports", 8, TournamentType.ELIMINATION_DIRECTE,
                2, 1, List.of(RankingCriteria.POINTS, RankingCriteria.GOAL_DIFFERENCE), 7, 40, "18:00"));
        registerAll(teams, cup.getId(), tournamentService);

        // 4) Tournoi à 5 équipes : déclenche le tour préliminaire et les exempts.
        Tournament small = tournamentService.create(new TournamentRequest(
                "Trophée des Cités", Sport.HANDBALL,
                "Cinq participants : un match de tour préliminaire et trois exempts.",
                LocalDate.now().plusDays(10), null,
                "Gymnase municipal", 5, TournamentType.ELIMINATION_DIRECTE,
                2, 1, List.of(RankingCriteria.POINTS, RankingCriteria.GOALS_SCORED), 10, 40, "19:00"));
        registerAll(teams.subList(0, 5), small.getId(), tournamentService, true);
        matchService.generateMatches(small.getId(), null);
    }

    private void registerAll(List<Team> teams, Long tournamentId, TournamentService tournamentService) {
        registerAll(teams, tournamentId, tournamentService, false);
    }

    private void registerAll(List<Team> teams, Long tournamentId, TournamentService tournamentService,
                             boolean programImmediately) {
        // Les inscriptions sont d'abord posées sur le tournoi en brouillon (étape autorisée même
        // si la date de début est déjà passée), puis les inscriptions sont ouvertes.
        int seed = 1;
        for (Team team : teams) {
            tournamentService.registerTeam(tournamentId, team.getId(), seed++);
        }
        tournamentService.changeStatus(tournamentId, TournamentStatus.INSCRIPTION);
        if (programImmediately) {
            tournamentService.changeStatus(tournamentId, TournamentStatus.PROGRAMME);
        }
    }

    private void playFirstLeg(Long tournamentId, MatchService matchService) {
        List<int[]> scores = List.of(new int[]{2, 1}, new int[]{3, 0}, new int[]{1, 1}, new int[]{0, 2});
        List<com.example.tournament.entity.Match> firstRound = matchService.findByTournament(tournamentId).stream()
                .filter(m -> m.getRoundNumber() != null && m.getRoundNumber() == 1)
                .toList();
        for (int i = 0; i < firstRound.size(); i++) {
            int[] score = scores.get(i % scores.size());
            matchService.saveResult(firstRound.get(i).getId(),
                    new MatchResultRequest(score[0], score[1], null, null, null, null, null));
        }
    }
}
