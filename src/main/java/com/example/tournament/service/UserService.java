package com.example.tournament.service;

import com.example.tournament.dto.request.TournamentRequest;
import com.example.tournament.dto.response.UserResponse;
import com.example.tournament.entity.Role;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestion des utilisateurs (réservée au rôle ADMIN). */
public interface UserService {

    @Transactional(readOnly = true)
    List<UserResponse> findAll();

    @Transactional
    UserResponse create(String username, String rawPassword, String fullName, String email, Role role);

    @Transactional
    UserResponse changeRole(Long id, Role role);

    @Transactional
    void delete(Long id);
}
