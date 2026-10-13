
package fr.dawan.cc_java_mongo.config;

import fr.dawan.cc_java_mongo.model.Utilisateur;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Crée facultativement un compte de démonstration pour le développement. */
@Configuration
public class BootstrapUserConfig {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapUserConfig.class);

    @Bean
    @ConditionalOnProperty(
            name = "app.security.bootstrap-user.enabled",
            havingValue = "true")
    public ApplicationRunner bootstrapUser(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.bootstrap-user.username}") String username,
            @Value("${app.security.bootstrap-user.password}") String password) {

        return args -> {
            if (utilisateurRepository.findByUsername(username).isEmpty()) {
                Utilisateur utilisateur = new Utilisateur(
                        null,
                        username,
                        passwordEncoder.encode(password),
                        "USER");

                utilisateurRepository.save(utilisateur);
                logger.info("Compte de démonstration créé pour : {}", username);
            }
        };
    }
}

