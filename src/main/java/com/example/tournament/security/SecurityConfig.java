package com.example.tournament.security;

import com.example.tournament.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de Spring Security.
 *
 * <p>Deux chaînes de filtres distinctes :</p>
 * <ul>
 *     <li>{@code /api/**} : authentification HTTP Basic, sans état, sans CSRF ;</li>
 *     <li>le reste : authentification par formulaire avec session.</li>
 * </ul>
 *
 * <p>Les droits sont différents selon le rôle : ADMIN (configuration), MANAGER (gestion métier),
 * OPERATOR (opérations quotidiennes), USER (consultation).</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Chaîne dédiée à l'API REST. */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "OPERATOR", "MANAGER", "ADMIN")
                        // Gestion des équipes et joueurs : MANAGER ou ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/teams", "/api/teams/*/players").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/teams/**", "/api/teams/*/players/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/teams/**").hasAnyRole("MANAGER", "ADMIN")
                        // Gestion des tournois : MANAGER ou ADMIN
                        // La génération du calendrier reste accessible à l'opérateur,
                        // comme sur l'interface MVC (cf. webSecurityFilterChain).
                        .requestMatchers(HttpMethod.POST, "/api/tournaments/*/generate-matches")
                        .hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                        .requestMatchers("/api/tournaments/**").hasAnyRole("MANAGER", "ADMIN")
                        // Saisie de résultat et suivi : OPERATOR et au-dessus
                        .requestMatchers(HttpMethod.PUT, "/api/matches/*/result").hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/matches/**").hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/matches/**").hasAnyRole("MANAGER", "ADMIN")
                        // Utilisateurs : ADMIN uniquement
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("USER", "OPERATOR", "MANAGER", "ADMIN"))
                .httpBasic(basic -> {
                })
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\","
                                    + "\"code\":\"UNAUTHENTICATED\","
                                    + "\"message\":\"Authentification requise (Basic).\"}");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\","
                                    + "\"code\":\"ACCESS_DENIED\","
                                    + "\"message\":\"Vous n'avez pas les droits nécessaires.\"}");
                        }));
        return http.build();
    }

    /** Chaîne dédiée à l'interface MVC Thymeleaf. */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/error", "/error/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/tournaments/new", "/tournaments/*/edit", "/tournaments/*/delete",
                                "/teams/new", "/teams/*/edit", "/teams/*/delete").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/tournaments", "/tournaments/*", "/tournaments/*/status",
                                "/teams", "/teams/*").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/matches/*/result", "/tournaments/*/generate").hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                        .anyRequest().hasAnyRole("USER", "OPERATOR", "MANAGER", "ADMIN"))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .exceptionHandling(handling -> handling
                        .accessDeniedPage("/error/403"));
        return http.build();
    }
}
