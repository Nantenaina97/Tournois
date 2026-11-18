package com.example.tournament.mapper;

import com.example.tournament.dto.request.PlayerRequest;
import com.example.tournament.dto.request.TeamRequest;
import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.request.TournamentUpdateRequest;
import com.example.tournament.entity.Player;
import com.example.tournament.entity.RankingCriteria;
import com.example.tournament.entity.Team;
import com.example.tournament.entity.Tournament;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Convertisseur DTO → entité (création et mise à jour). */
@Component
public class RequestMapper {

    public Tournament toEntity(TournamentRequest request) {
        Tournament tournament = new Tournament();
        applyCreate(request, tournament);
        return tournament;
    }

    public void applyCreate(TournamentRequest request, Tournament tournament) {
        tournament.setName(request.name().trim());
        tournament.setSport(request.sport());
        tournament.setDescription(request.description());
        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
        tournament.setVenue(request.venue());
        tournament.setMaxTeams(request.maxTeams());
        tournament.setType(request.type());
        if (request.pointsForWin() != null) {
            tournament.setPointsForWin(request.pointsForWin());
        }
        if (request.pointsForDraw() != null) {
            tournament.setPointsForDraw(request.pointsForDraw());
        }
        if (request.daysBetweenRounds() != null) {
            tournament.setDaysBetweenRounds(request.daysBetweenRounds());
        }
        if (request.matchDurationMinutes() != null) {
            tournament.setMatchDurationMinutes(request.matchDurationMinutes());
        }
        if (request.kickoffTime() != null) {
            tournament.setKickoffTime(request.kickoffTime());
        }
        if (request.rankingCriteria() != null && !request.rankingCriteria().isEmpty()) {
            List<RankingCriteria> criteria = new ArrayList<>(request.rankingCriteria());
            if (!criteria.contains(RankingCriteria.POINTS)) {
                criteria.add(0, RankingCriteria.POINTS);
            }
            tournament.setRankingCriteria(criteria);
        }
    }

    public void applyUpdate(TournamentUpdateRequest request, Tournament tournament) {
        tournament.setName(request.name().trim());
        tournament.setDescription(request.description());
        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
        tournament.setVenue(request.venue());
        tournament.setMaxTeams(request.maxTeams());
        tournament.setPointsForWin(request.pointsForWin());
        tournament.setPointsForDraw(request.pointsForDraw());
        tournament.setDaysBetweenRounds(request.daysBetweenRounds());
        tournament.setMatchDurationMinutes(request.matchDurationMinutes());
        tournament.setKickoffTime(request.kickoffTime());
        if (request.rankingCriteria() != null && !request.rankingCriteria().isEmpty()) {
            List<RankingCriteria> criteria = new ArrayList<>(request.rankingCriteria());
            if (!criteria.contains(RankingCriteria.POINTS)) {
                criteria.add(0, RankingCriteria.POINTS);
            }
            tournament.setRankingCriteria(criteria);
        }
    }

    public Team toEntity(TeamRequest request) {
        Team team = new Team();
        applyTeam(request, team);
        return team;
    }

    public void applyTeam(TeamRequest request, Team team) {
        team.setName(request.name().trim());
        team.setLogoUrl(request.logoUrl());
        team.setCity(request.city());
        team.setCoach(request.coach());
        team.setCoachPhone(request.coachPhone());
        team.setFoundedYear(request.foundedYear());
        if (request.status() != null) {
            team.setStatus(request.status());
        }
    }

    public Player toEntity(PlayerRequest request) {
        Player player = new Player();
        applyPlayer(request, player);
        return player;
    }

    public void applyPlayer(PlayerRequest request, Player player) {
        player.setFirstName(request.firstName().trim());
        player.setLastName(request.lastName().trim());
        player.setPosition(request.position());
        player.setJerseyNumber(request.jerseyNumber());
        player.setBirthDate(request.birthDate());
        player.setNationality(request.nationality());
    }
}
