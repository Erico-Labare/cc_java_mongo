package fr.dawan.cc_java_mongo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure la documentation OpenAPI de l'application.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medicalApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Medical API")
                        .version("1.0.0")
                        .description(
                                "API REST de gestion des patients, médecins, "
                                        + "médicaments et consultations."));
    }
}