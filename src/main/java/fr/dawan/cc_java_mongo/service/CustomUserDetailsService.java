package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.model.Utilisateur;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Charge les informations d'un utilisateur depuis MongoDB. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(
            UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

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
