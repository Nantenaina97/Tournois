package com.example.tournament;

import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TournamentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Parcours applicatif complet (jeu de données de démonstration)")
class TournamentApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Test
    @DisplayName("le jeu de données de démonstration est créé au démarrage")
    void demoDataIsSeeded() {
        assertThat(tournamentRepository.count()).isGreaterThanOrEqualTo(4);
        assertThat(tournamentRepository.findAll())
                .extracting(Tournament::getName)
                .contains("Coupe de Ligue 2026", "Régional U19 — Poule A",
                        "Coupe Nationale — Phase finale", "Trophée des Cités");
        assertThat(tournamentRepository.findAll())
                .extracting(Tournament::getType)
                .contains(TournamentType.CHAMPIONNAT_ALLER_RETOUR, TournamentType.ELIMINATION_DIRECTE);
    }

    // ------------------------------------------------------------------- Sécurité

    @Test
    @DisplayName("la page de connexion est accessible sans authentification")
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Connexion")));
    }

    @Test
    @DisplayName("le tableau de bord exige une authentification")
    void dashboardRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("un utilisateur simple ne peut pas administrer les comptes")
    void simpleUserCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("l'API refuse un accès anonyme avec une erreur JSON 401")
    void apiRequiresBasicAuthentication() throws Exception {
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    // ------------------------------------------------------------ Interface MVC

    @Test
    @DisplayName("le tableau de bord s'affiche pour un utilisateur authentifié")
    void dashboardRenders() throws Exception {
        mockMvc.perform(get("/dashboard").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tableau de bord")));
    }

    @Test
    @DisplayName("la liste des tournois s'affiche avec ses compteurs")
    void tournamentListRenders() throws Exception {
        mockMvc.perform(get("/tournaments").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Coupe de Ligue 2026")));
    }

    @Test
    @DisplayName("toutes les pages de détail d'un tournoi s'affichent")
    void tournamentDetailPagesRender() throws Exception {
        List<Tournament> tournaments = tournamentRepository.findAll();

        for (Tournament tournament : tournaments) {
            Long id = tournament.getId();
            mockMvc.perform(get("/tournaments/" + id).with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(tournament.getName())));

            mockMvc.perform(get("/tournaments/" + id + "/teams").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/tournaments/" + id + "/matches").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/tournaments/" + id + "/statistics").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/tournaments/" + id + "/generate").with(SecurityMockMvcRequestPostProcessors.user("manager").roles("MANAGER")))
                    .andExpect(status().isOk());

            if (tournament.getType() == TournamentType.ELIMINATION_DIRECTE) {
                mockMvc.perform(get("/tournaments/" + id + "/bracket").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                        .andExpect(status().isOk());
            } else {
                mockMvc.perform(get("/tournaments/" + id + "/ranking").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                        .andExpect(status().isOk());
            }
        }
    }

    @Test
    @DisplayName("le formulaire de création de tournoi s'affiche pour un manager")
    void tournamentFormRenders() throws Exception {
        mockMvc.perform(get("/tournaments/new").with(SecurityMockMvcRequestPostProcessors.user("manager").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Créer un tournoi")));
    }

    @Test
    @DisplayName("les pages équipes s'affichent")
    void teamPagesRender() throws Exception {
        mockMvc.perform(get("/teams").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/teams/1").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/teams/new").with(SecurityMockMvcRequestPostProcessors.user("manager").roles("MANAGER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("la page d'administration des utilisateurs s'affiche pour un administrateur")
    void adminUsersPageRenders() throws Exception {
        mockMvc.perform(get("/admin/users").with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Utilisateurs")));
    }

    @Test
    @DisplayName("un gestionnaire est renvoyé vers la page 403 personnalisée")
    void managerIsForbiddenOnAdminPages() throws Exception {
        mockMvc.perform(get("/admin/users").with(SecurityMockMvcRequestPostProcessors.user("manager").roles("MANAGER")))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    @DisplayName("la page 403 personnalisée s'affiche")
    void forbiddenPageRenders() throws Exception {
        mockMvc.perform(get("/error/403"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Accès refusé")));
    }

    @Test
    @DisplayName("la page 403 accepte aussi les forward non-GET")
    void forbiddenPageAcceptsForwardedPost() throws Exception {
        // Un refus de sécurité forwarde la requête d'origine en conservant sa méthode :
        // si la page 403 n'accepte que GET/HEAD, un formulaire refusé vaut 405.
        // Le forward intervient après le filtre CSRF ; une requête directe doit donc
        // fournir un jeton pour passer la sécurité.
        mockMvc.perform(post("/error/403").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Accès refusé")));
    }

    @Test
    @DisplayName("un formulaire refusé pour cause de CSRF renvoie 403 et non 405")
    void deniedPostIsNotReportedAsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/tournaments")
                        .with(SecurityMockMvcRequestPostProcessors.user("manager").roles("MANAGER"))
                        .param("_csrf", "jeton-caduce"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    @DisplayName("un utilisateur simple ne peut pas créer un tournoi via le formulaire MVC")
    void simpleUserCannotCreateTournamentThroughWebForm() throws Exception {
        mockMvc.perform(post("/tournaments")
                        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("name", "Tournoi interdit MVC")
                        .param("sport", "FOOTBALL")
                        .param("startDate", "2030-01-01")
                        .param("maxTeams", "8")
                        .param("type", "CHAMPIONNAT_SIMPLE")
                        .param("status", "BROUILLON"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    @DisplayName("un utilisateur simple ne peut pas créer une équipe via le formulaire MVC")
    void simpleUserCannotCreateTeamThroughWebForm() throws Exception {
        mockMvc.perform(post("/teams")
                        .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("name", "Équipe interdite MVC")
                        .param("city", "Paris")
                        .param("status", "ACTIF"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    @DisplayName("un manager peut créer un tournoi via le formulaire MVC")
    void managerCanCreateTournamentThroughWebForm() throws Exception {
        long before = tournamentRepository.count();

        mockMvc.perform(post("/tournaments")
                        .with(SecurityMockMvcRequestPostProcessors.user("manager").roles("MANAGER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("name", "Tournoi créé par le formulaire")
                        .param("sport", "HANDBALL")
                        .param("startDate", "2030-01-01")
                        .param("maxTeams", "8")
                        .param("type", "CHAMPIONNAT_SIMPLE")
                        .param("status", "BROUILLON"))
                .andExpect(status().is3xxRedirection());

        assertThat(tournamentRepository.count()).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("une ressource inexistante renvoie une page 404 lisible")
    void unknownTournamentRendersNotFound() throws Exception {
        mockMvc.perform(get("/tournaments/999999").with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Ressource introuvable")));
    }

    // Note : la page 404 servie par le conteneur pour une URL inconnue (via
    // NotFoundErrorPageRegistrar) n'est pas testable avec MockMvc, qui ne reproduit pas
    // le mécanisme de page d'erreur du conteneur de servlet.

    @Test
    @DisplayName("un corps JSON illisible renvoie une erreur 400 et non 500")
    void apiRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .with(httpBasic("manager", "manager123"))
                        .contentType("application/json")
                        .content("{\"name\": \"Tournoi\", \"rankingCriteria\": \"POINTS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
    }

    // ------------------------------------------------------------------ API REST

    @Test
    @DisplayName("l'API renvoie la liste des tournois au format JSON")
    void apiReturnsTournamentList() throws Exception {
        mockMvc.perform(get("/api/tournaments").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.content[*].name",
                        org.hamcrest.Matchers.hasItems("Coupe de Ligue 2026")))
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.content[0].status").exists());
    }

    @Test
    @DisplayName("le point de terminaison de santé est public")
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("le classement d'un championnat est exposé par l'API")
    void apiExposesRanking() throws Exception {
        Tournament league = tournamentRepository.findAll().stream()
                .filter(t -> t.getType() == TournamentType.CHAMPIONNAT_ALLER_RETOUR)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/tournaments/" + league.getId() + "/ranking").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(8)))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].teamName").exists());
    }

    @Test
    @DisplayName("l'opérateur accède à la génération du calendrier via l'API mais pas l'utilisateur simple")
    void apiGenerateMatchesIsOperatorFriendly() throws Exception {
        Tournament league = tournamentRepository.findAll().stream()
                .filter(t -> t.getType() == TournamentType.CHAMPIONNAT_SIMPLE)
                .findFirst()
                .orElseThrow();

        // L'opérateur n'est pas bloqué par la sécurité (422 = règle métier si déjà généré).
        mockMvc.perform(post("/api/tournaments/" + league.getId() + "/generate-matches")
                        .with(httpBasic("operateur", "operateur123"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(403));

        mockMvc.perform(post("/api/tournaments/" + league.getId() + "/generate-matches")
                        .with(httpBasic("user", "user123"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("l'opérateur saisit un résultat via l'API mais pas l'utilisateur simple")
    void apiMatchResultIsOperatorFriendly() throws Exception {
        Tournament league = tournamentRepository.findAll().stream()
                .filter(t -> t.getType() == TournamentType.CHAMPIONNAT_SIMPLE)
                .findFirst()
                .orElseThrow();
        Long matchId = matchRepository
                .findByTournamentIdOrderByRoundNumberAscBracketPositionAsc(league.getId())
                .get(0)
                .getId();

        // La saisie est réservée à l'opérateur ; un 422 dû à une règle métier
        // (tournoi clos, équipes non déterminées) reste la preuve que la
        // sécurité a bien laissé passer la requête.
        mockMvc.perform(put("/api/matches/" + matchId + "/result")
                        .with(httpBasic("operateur", "operateur123"))
                        .contentType("application/json")
                        .content("{\"homeScore\": 2, \"awayScore\": 1}"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(403));

        mockMvc.perform(put("/api/matches/" + matchId + "/result")
                        .with(httpBasic("user", "user123"))
                        .contentType("application/json")
                        .content("{\"homeScore\": 0, \"awayScore\": 0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un utilisateur simple ne peut pas créer un tournoi via l'API")
    void apiRejectsCreationForSimpleUser() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .with(httpBasic("user", "user123"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Tournoi interdit",
                                  "sport": "FOOTBALL",
                                  "startDate": "2030-01-01",
                                  "maxTeams": 8,
                                  "type": "CHAMPIONNAT_SIMPLE"
                                }"""))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un manager peut créer un tournoi via l'API")
    void apiAllowsCreationForManager() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .with(httpBasic("manager", "manager123"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Tournoi de test API",
                                  "sport": "HANDBALL",
                                  "description": "Créé par le test d'intégration",
                                  "startDate": "2030-01-01",
                                  "maxTeams": 4,
                                  "type": "CHAMPIONNAT_SIMPLE",
                                  "pointsForWin": 3,
                                  "pointsForDraw": 1
                                }"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("BROUILLON"));
    }

    @Test
    @DisplayName("une erreur métier est renvoyée avec le code 422 et un message explicite")
    void businessErrorIsReported() throws Exception {
        Tournament league = tournamentRepository.findAll().stream()
                .filter(t -> t.getType() == TournamentType.CHAMPIONNAT_ALLER_RETOUR)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/api/tournaments/" + league.getId() + "/generate-matches")
                        .with(httpBasic("manager", "manager123"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("calendrier")));
    }

    @Test
    @DisplayName("les statistiques agrégées sont exposées par l'API")
    void apiExposesStatistics() throws Exception {
        Tournament league = tournamentRepository.findAll().get(0);

        mockMvc.perform(get("/api/statistics/tournaments/" + league.getId())
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMatches").isNumber())
                .andExpect(jsonPath("$.finishedMatches").isNumber());
    }
}
