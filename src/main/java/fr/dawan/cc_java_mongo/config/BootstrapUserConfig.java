
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

/**
 * Configuration Spring pour créer facultativement un compte utilisateur de démonstration.
 * <p>
 * Cette configuration est activée uniquement si la propriété
 * {@code app.security.bootstrap-user.enabled} est définie à {@code true}.
 * Elle permet de créer automatiquement un utilisateur par défaut lors du démarrage
 * de l'application, utile pour les environnements de développement.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Utilisateur
 * @see fr.dawan.cc_java_mongo.repository.UtilisateurRepository
 */
@Configuration
public class BootstrapUserConfig {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapUserConfig.class);

    /**
     * Crée un {@link ApplicationRunner} qui initialise un utilisateur de démonstration
     * si celui-ci n'existe pas déjà.
     *
     * @param utilisateurRepository repository pour accéder aux utilisateurs
     * @param passwordEncoder encodeur de mot de passe
     * @param username nom d'utilisateur configuré via {@code app.security.bootstrap-user.username}
     * @param password mot de passe configuré via {@code app.security.bootstrap-user.password}
     * @return un ApplicationRunner qui crée l'utilisateur si nécessaire
     */
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

