
package fr.dawan.cc_java_mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration Spring pour le chiffrement des mots de passe.
 * <p>
 * Cette configuration fournit un {@link PasswordEncoder} utilisant l'algorithme BCrypt
 * pour chiffrer et vérifier les mots de passe des utilisateurs.
 * </p>
 *
 * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 */
@Configuration
public class PasswordConfig {

    /**
     * Crée un encodeur de mot de passe utilisant l'algorithme BCrypt.
     *
     * @return l'encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}