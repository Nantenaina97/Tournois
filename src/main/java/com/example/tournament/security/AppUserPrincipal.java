package com.example.tournament.security;

import com.example.tournament.entity.AppUser;
import com.example.tournament.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal Spring Security enrichi du rôle applicatif.
 * Permet d'afficher le rôle dans l'interface Thymeleaf.
 */
public class AppUserPrincipal extends User {

    private final Long id;
    private final String fullName;
    private final Role role;

    public AppUserPrincipal(AppUser user) {
        super(user.getUsername(), user.getPassword(), authoritiesOf(user.getRole()));
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.role = user.getRole();
    }

    private static List<GrantedAuthority> authoritiesOf(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public String getDisplayName() {
        return fullName != null && !fullName.isBlank() ? fullName : getUsername();
    }

    public static UserDetails from(AppUser user) {
        return new AppUserPrincipal(user);
    }

    /** Raccourci pour les tests. */
    public static Collection<GrantedAuthority> authorities(String roleName) {
        return authoritiesOf(Role.valueOf(roleName));
    }
}
