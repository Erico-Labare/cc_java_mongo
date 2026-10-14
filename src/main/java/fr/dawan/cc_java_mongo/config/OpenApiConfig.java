package fr.dawan.cc_java_mongo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring pour OpenAPI/Swagger.
 * <p>
 * Cette classe configure la documentation automatique de l'API REST via OpenAPI 3.
 * Elle définit les informations de l'API et le schéma de sécurité JWT requis
 * pour l'authentification des endpoints.
 * </p>
 *
 * @see io.swagger.v3.oas.models.OpenAPI
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configure le bean OpenAPI pour la documentation de l'API.
     * <p>
     * Définit le titre, la version, la description de l'API et le schéma de sécurité
     * basé sur les jetons JWT transmis via l'en-tête {@code Authorization: Bearer}.
     * </p>
     *
     * @return la configuration OpenAPI de l'application
     */
    @Bean
    public OpenAPI medicalApiOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Medical API")
                        .version("1.0.0")
                        .description(
                                "API REST de gestion des patients, médecins, "
                                        + "médicaments et consultations."))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName));
    }
}
