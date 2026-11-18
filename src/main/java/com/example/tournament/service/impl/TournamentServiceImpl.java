package com.example.tournament.service.impl;

import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.request.TournamentUpdateRequest;
import com.example.tournament.entity.RegistrationStatus;
import com.example.tournament.entity.Sport;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.TeamRegistration;
import com.example.tournament.entity.TeamStatus;
import com.example.tournament.entity.Tournament;
import com.example.tournament.entity.TournamentStatus;
import com.example.tournament.entity.TournamentType;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.mapper.RequestMapper;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TeamRegistrationRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.TournamentRepository;
import com.example.tournament.service.TournamentService;
import com.example.tournament.util.TournamentSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implémentation de la gestion des tournois.
 *
 * <p>Toutes les règles de gestion liées au statut du tournoi sont centralisées ici.</p>
 */
@Service
public class TournamentServiceImpl implements TournamentService {

    /** Transitions de statut autorisées. */
    private static final Map<TournamentStatus, Set<TournamentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(Map.of(
            TournamentStatus.BROUILLON, EnumSet.of(TournamentStatus.INSCRIPTION, TournamentStatus.PROGRAMME, TournamentStatus.ANNULE),
            TournamentStatus.INSCRIPTION, EnumSet.of(TournamentStatus.BROUILLON, TournamentStatus.PROGRAMME, TournamentStatus.ANNULE),
            TournamentStatus.PROGRAMME, EnumSet.of(TournamentStatus.INSCRIPTION, TournamentStatus.EN_COURS, TournamentStatus.ANNULE),
            TournamentStatus.EN_COURS, EnumSet.of(TournamentStatus.TERMINE, TournamentStatus.ANNULE),
            TournamentStatus.TERMINE, EnumSet.noneOf(TournamentStatus.class),
            TournamentStatus.ANNULE, EnumSet.noneOf(TournamentStatus.class)
    ));

    private static final long MIN_TEAMS_TO_PROGRAM = 2;

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final TeamRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final RequestMapper requestMapper;

    public TournamentServiceImpl(TournamentRepository tournamentRepository,
                                 TeamRepository teamRepository,
                                 TeamRegistrationRepository registrationRepository,
                                 MatchRepository matchRepository,
                                 RequestMapper requestMapper) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.requestMapper = requestMapper;
    }

    // ------------------------------------------------------------------ Lecture

    @Override
    @Transactional(readOnly = true)
    public Page<Tournament> search(String search, TournamentStatus status, TournamentType type, Sport sport,
                                   LocalDate from, LocalDate to, Pageable pageable) {
        Specification<Tournament> specification = TournamentSpecifications.all(
                TournamentSpecifications.search(search),
                TournamentSpecifications.hasStatus(status),
                TournamentSpecifications.hasType(type),
                TournamentSpecifications.hasSport(sport),
                TournamentSpecifications.startsAfter(from),
                TournamentSpecifications.startsBefore(to));
        return tournamentRepository.findAll(specification, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tournament> findById(Long id) {
        return tournamentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tournament> findAll() {
        return tournamentRepository.findAllByOrderByStartDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(TournamentStatus status) {
        return tournamentRepository.countByStatus(status);
    }

    // ---------------------------------------------------------------- Écriture

    @Override
    @Transactional
    public Tournament create(TournamentRequest request) {
        if (tournamentRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Un tournoi nommé « " + request.name().trim() + " » existe déjà.");
        }
        Tournament tournament = requestMapper.toEntity(request);
        return tournamentRepository.save(tournament);
    }

    @Override
    @Transactional
    public Tournament update(Long id, TournamentUpdateRequest request) {
        Tournament tournament = getRequired(id);
        if (tournament.getStatus().isClosed()) {
            throw new InvalidOperationException(
                    "Le tournoi « " + tournament.getName() + " » est " + tournament.getStatus().getLibelle().toLowerCase()
                            + " : aucune modification n'est possible.");
        }
        if (!tournament.getName().equalsIgnoreCase(request.name().trim())
                && tournamentRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Un tournoi nommé « " + request.name().trim() + " » existe déjà.");
        }
        if (tournament.getStatus() == TournamentStatus.EN_COURS
                && !tournament.getStatus().equals(request.status())) {
            throw new InvalidOperationException(
                    "Le statut d'un tournoi en cours ne peut pas être modifié directement.");
        }
        int registered = (int) registrationRepository.countByTournamentIdAndStatus(id, RegistrationStatus.INSCRITE);
        if (request.maxTeams() < registered) {
            throw new InvalidOperationException(
                    "Le nombre maximum d'équipes (" + request.maxTeams() + ") est inférieur au nombre d'équipes déjà inscrites ("
                            + registered + ").");
        }
        requestMapper.applyUpdate(request, tournament);
        return tournamentRepository.save(tournament);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Tournament tournament = getRequired(id);
        if (tournament.getStatus() == TournamentStatus.EN_COURS) {
            throw new InvalidOperationException(
                    "Impossible de supprimer un tournoi en cours. Clôturez-le d'abord.");
        }
        if (tournament.getStatus() == TournamentStatus.TERMINE) {
            throw new InvalidOperationException(
                    "Un tournoi terminé ne peut pas être supprimé (traçabilité des résultats).");
        }
        matchRepository.deleteAll(matchRepository.findByTournamentIdOrderByRoundNumberAscBracketPositionAsc(id));
        registrationRepository.deleteAll(registrationRepository.findByTournamentId(id));
        tournamentRepository.delete(tournament);
    }

    @Override
    @Transactional
    public Tournament changeStatus(Long id, TournamentStatus newStatus) {
        Tournament tournament = getRequired(id);
        TournamentStatus current = tournament.getStatus();

        if (current == newStatus) {
            return tournament;
        }
        Set<TournamentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidOperationException("Transition impossible : " + current.getLibelle()
                    + " → " + newStatus.getLibelle() + ".");
        }
        if (newStatus == TournamentStatus.PROGRAMME) {
            long registered = registrationRepository.countByTournamentIdAndStatus(id, RegistrationStatus.INSCRITE);
            if (registered < MIN_TEAMS_TO_PROGRAM) {
                throw new InvalidOperationException(
                        "Au moins " + MIN_TEAMS_TO_PROGRAM + " équipes doivent être inscrites pour programmer un tournoi.");
            }
        }
        if (newStatus == TournamentStatus.INSCRIPTION && matchRepository.existsByTournamentId(id)) {
            throw new InvalidOperationException(
                    "Les rencontres ont déjà été générées : impossible de rouvrir les inscriptions.");
        }
        tournament.setStatus(newStatus);
        return tournamentRepository.save(tournament);
    }

    // ------------------------------------------------------------- Inscriptions

    @Override
    @Transactional
    public TeamRegistration registerTeam(Long tournamentId, Long teamId, Integer seedNumber) {
        Tournament tournament = getRequired(tournamentId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> ResourceNotFoundException.of("Équipe", teamId));

        if (!tournament.getStatus().isRegistrationOpen()) {
            throw new InvalidOperationException("Les inscriptions sont fermées pour le tournoi « "
                    + tournament.getName() + " » (statut : " + tournament.getStatus().getLibelle() + ").");
        }
        if (tournament.getStatus() == TournamentStatus.ANNULE) {
            throw new InvalidOperationException("Un tournoi annulé ne peut plus accueillir d'équipe.");
        }
        if (tournament.getStartDate() != null && tournament.getStartDate().isBefore(LocalDate.now())
                && tournament.getStatus() != TournamentStatus.BROUILLON) {
            throw new InvalidOperationException("Le tournoi a déjà commencé : aucune nouvelle inscription n'est acceptée.");
        }
        if (team.getStatus() == TeamStatus.DISQUALIFIE) {
            throw new InvalidOperationException("L'équipe « " + team.getName() + " » est disqualifiée.");
        }
        if (registrationRepository.existsByTournamentIdAndTeamId(tournamentId, teamId)) {
            throw new ConflictException("L'équipe « " + team.getName() + " » est déjà inscrite à ce tournoi.");
        }
        long registered = registrationRepository.countByTournamentIdAndStatus(tournamentId, RegistrationStatus.INSCRITE);
        if (registered >= tournament.getMaxTeams()) {
            throw new InvalidOperationException("Le nombre maximum d'équipes (" + tournament.getMaxTeams()
                    + ") est atteint pour ce tournoi.");
        }
        Integer seed = seedNumber != null ? seedNumber : (int) registered + 1;
        TeamRegistration registration = new TeamRegistration(tournament, team, seed);
        return registrationRepository.save(registration);
    }

    @Override
    @Transactional
    public void unregisterTeam(Long tournamentId, Long teamId) {
        Tournament tournament = getRequired(tournamentId);
        TeamRegistration registration = registrationRepository.findByTournamentIdAndTeamId(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "L'équipe " + teamId + " n'est pas inscrite au tournoi " + tournamentId + "."));

        if (tournament.getStatus().isClosed()) {
            throw new InvalidOperationException("Le tournoi est " + tournament.getStatus().getLibelle().toLowerCase()
                    + " : le retrait d'une équipe n'est plus possible.");
        }
        if (tournament.getStatus() == TournamentStatus.EN_COURS) {
            throw new InvalidOperationException(
                    "Le tournoi est en cours : une équipe ne peut plus se retirer.");
        }
        if (registration.getStatus() != RegistrationStatus.INSCRITE) {
            throw new InvalidOperationException("Cette équipe n'est plus active dans le tournoi.");
        }

        boolean hasPlayedMatch = matchRepository.findRealMatches(tournamentId).stream()
                .anyMatch(m -> (m.getHomeTeam() != null && m.getHomeTeam().getId().equals(teamId))
                        || (m.getAwayTeam() != null && m.getAwayTeam().getId().equals(teamId)));
        if (hasPlayedMatch) {
            throw new InvalidOperationException(
                    "L'équipe a déjà disputé une rencontre : elle ne peut plus être retirée du tournoi.");
        }
        registrationRepository.delete(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamRegistration> getRegistrations(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw ResourceNotFoundException.of("Tournoi", tournamentId);
        }
        return registrationRepository.findByTournamentIdOrderByIdAsc(tournamentId);
    }

    // ------------------------------------------------------------------ Utilitaire

    private Tournament getRequired(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Tournoi", id));
    }
}
