package fr.dawan.cc_java_mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration Spring pour la gestion des accès cross-origin (CORS).
 * <p>
 * Cette configuration autorise les requêtes provenant des origines front-end
 * spécifiées (localhost sur les ports 3000, 4200 et 5173) avec les méthodes HTTP
 * et en-têtes nécessaires pour le fonctionnement de l'API REST.
 * </p>
 */
@Configuration
public class CorsConfig {

    /**
     * Configure la source de configuration CORS pour l'application.
     * <p>
     * Autorise les requêtes depuis localhost sur les ports de développement courants,
     * avec les méthodes standards REST et les en-têtes d'authentification et de contenu.
     * </p>
     *
     * @return la source de configuration CORS appliquée à tous les chemins ({@code /**})
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept"
        ));

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
