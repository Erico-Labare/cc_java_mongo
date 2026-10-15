package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.model.Role;
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
 * <p>
 * Le rôle de l'utilisateur est extrait de l'énumération {@link Role} et converti
 * en autorité Spring Security avec le préfixe ROLE_.
 * </p>
 *
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see fr.dawan.cc_java_mongo.model.Utilisateur
 * @see fr.dawan.cc_java_mongo.model.Role
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
     * Recherche l'utilisateur dans MongoDB, extrait son rôle depuis l'énumération
     * {@link Role} et construit un objet UserDetails Spring Security.
     * Si le rôle est null, le rôle PATIENT est appliqué par défaut.
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

        Role role = utilisateur.getRole();

        if (role == null) {
            role = Role.PATIENT;
        }

        return User.withUsername(utilisateur.getUsername())
                .password(utilisateur.getPassword())
                .roles(role.name())
                .build();
    }
}
