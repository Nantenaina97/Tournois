package com.example.tournament.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration web : ressources statiques et assets (logos, feuilles de style).
 *
 * <p>Les pages d'erreur personnalisées ne sont pas déclarées ici : les
 * {@code viewController} n'acceptent que GET et HEAD, alors que ces pages sont
 * atteintes par {@code forward} à la suite d'un refus de sécurité ou d'une erreur,
 * donc avec la méthode HTTP d'origine. Elles sont exposées par
 * {@code ErrorPageWebController}.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }
}
