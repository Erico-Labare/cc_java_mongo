
package fr.dawan.cc_java_mongo.config;

import fr.dawan.cc_java_mongo.model.Role;
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
 * Configuration Spring pour créer facultativement un compte utilisateur initial.
 * <p>
 * Cette configuration est activée uniquement si la propriété
 * {@code app.security.bootstrap-user.enabled} est définie à {@code true}.
 * Elle permet de créer automatiquement un utilisateur administrateur lors du démarrage
 * de l'application, utile pour les environnements de développement.
 * </p>
 * <p>
 * <strong>Attention :</strong> Ce mécanisme ne doit être utilisé qu'en développement.
 * En production, désactivez cette fonctionnalité et utilisez une procédure
 * sécurisée de création des comptes administrateurs.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Utilisateur
 * @see fr.dawan.cc_java_mongo.repository.UtilisateurRepository
 * @see fr.dawan.cc_java_mongo.model.Role
 */
@Configuration
public class BootstrapUserConfig {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapUserConfig.class);

    /**
     * Crée un {@link ApplicationRunner} qui initialise un utilisateur administrateur
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
                if (password == null || password.isBlank()) {
                    logger.error("Impossible de créer le compte administrateur : mot de passe vide ou non défini.");
                    logger.error("Définissez la variable d'environnement APP_ADMIN_PASSWORD.");
                    throw new IllegalStateException(
                            "Le mot de passe de l'utilisateur bootstrap ne peut pas être vide.");
                }

                Utilisateur utilisateur = new Utilisateur(
                        null,
                        username,
                        passwordEncoder.encode(password),
                        Role.ADMIN);

                utilisateurRepository.save(utilisateur);
                logger.info("Compte administrateur initial créé pour : {}", username);
                logger.warn("Le bootstrap utilisateur est activé. Désactivez-le en production.");
            }
        };
    }
}

