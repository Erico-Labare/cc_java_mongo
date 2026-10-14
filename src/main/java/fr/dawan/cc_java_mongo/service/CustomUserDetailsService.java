package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.model.Utilisateur;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service Spring Security pour charger les détails d'un utilisateur depuis MongoDB.
 * <p>
 * Cette implémentation de {@link UserDetailsService} charge les informations
 * d'authentification d'un utilisateur à partir de la base MongoDB et les convertit
 * en un objet {@link UserDetails} compatible avec Spring Security.
 * </p>
 *
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see fr.dawan.cc_java_mongo.model.Utilisateur
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Construit le service avec le repository des utilisateurs.
     *
     * @param utilisateurRepository le repository pour accéder aux utilisateurs
     */
    public CustomUserDetailsService(
            UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Charge les détails de l'utilisateur par son nom d'utilisateur.
     * <p>
     * Recherche l'utilisateur dans MongoDB, normalise son rôle (supprime le préfixe
     * ROLE_ s'il est présent, applique USER par défaut si vide) et construit
     * un objet UserDetails Spring Security.
     * </p>
     *
     * @param username le nom d'utilisateur recherché
     * @return les détails de l'utilisateur pour Spring Security
     * @throws UsernameNotFoundException si l'utilisateur n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable : " + username));

        String role = utilisateur.getRole();

        if (role == null || role.isBlank()) {
            role = "USER";
        }

        // La méthode roles() ajoute automatiquement le préfixe ROLE_.
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        return User.withUsername(utilisateur.getUsername())
                .password(utilisateur.getPassword())
                .roles(role)
                .build();
    }
}
