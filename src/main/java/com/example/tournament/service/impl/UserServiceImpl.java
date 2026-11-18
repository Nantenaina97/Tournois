package com.example.tournament.service.impl;

import com.example.tournament.dto.response.UserResponse;
import com.example.tournament.entity.AppUser;
import com.example.tournament.entity.Role;
import com.example.tournament.exception.ConflictException;
import com.example.tournament.exception.InvalidOperationException;
import com.example.tournament.exception.ResourceNotFoundException;
import com.example.tournament.repository.AppUserRepository;
import com.example.tournament.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implémentation de la gestion des utilisateurs (réservée au rôle ADMIN). */
@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAllByOrderByUsernameAsc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse create(String username, String rawPassword, String fullName, String email, Role role) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("L'identifiant « " + username + " » est déjà utilisé.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new InvalidOperationException("Le mot de passe doit contenir au moins 6 caractères.");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role == null ? Role.USER : role);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse changeRole(Long id, Role role) {
        AppUser user = getRequired(id);
        if (user.getRole() == Role.ADMIN && role != Role.ADMIN) {
            long admins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.isEnabled())
                    .count();
            if (admins <= 1) {
                throw new InvalidOperationException("Impossible de rétrograder le dernier administrateur.");
            }
        }
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AppUser user = getRequired(id);
        if (user.getRole() == Role.ADMIN) {
            long admins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.isEnabled())
                    .count();
            if (admins <= 1) {
                throw new InvalidOperationException("Impossible de supprimer le dernier administrateur.");
            }
        }
        userRepository.delete(user);
    }

    private AppUser getRequired(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", id));
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getRole().getLibelle(),
                user.isEnabled());
    }
}
