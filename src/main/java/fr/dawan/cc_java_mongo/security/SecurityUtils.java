package fr.dawan.cc_java_mongo.security;

import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.model.Utilisateur;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilitaires pour la récupération des informations de sécurité.
 * <p>
 * Cette classe fournit des méthodes statiques pour accéder à l'utilisateur
 * authentifié, son rôle et ses associations métier (patient ou médecin).
 * </p>
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Classe utilitaire - constructeur privé
    }

    /**
     * Récupère l'authentification actuelle depuis le contexte Spring Security.
     *
     * @return l'authentification actuelle, ou null si non authentifié
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Récupère le nom d'utilisateur de l'utilisateur authentifié.
     *
     * @return le nom d'utilisateur, ou null si non authentifié
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Récupère l'utilisateur complet depuis la base de données.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @return l'utilisateur authentifié
     * @throws IllegalStateException si l'utilisateur n'est pas authentifié ou introuvable
     */
    public static Utilisateur getCurrentUser(UtilisateurRepository utilisateurRepository) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("Aucun utilisateur authentifié.");
        }
        return utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Utilisateur authentifié introuvable dans la base : " + username));
    }

    /**
     * Récupère le rôle de l'utilisateur authentifié.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @return le rôle de l'utilisateur
     * @throws IllegalStateException si l'utilisateur n'est pas authentifié
     */
    public static Role getCurrentRole(UtilisateurRepository utilisateurRepository) {
        Utilisateur utilisateur = getCurrentUser(utilisateurRepository);
        return utilisateur.getRole();
    }

    /**
     * Récupère l'identifiant patient de l'utilisateur authentifié.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @return l'identifiant patient, ou null si l'utilisateur n'est pas un patient
     * @throws IllegalStateException si l'utilisateur n'est pas authentifié
     */
    public static String getCurrentPatientId(UtilisateurRepository utilisateurRepository) {
        Utilisateur utilisateur = getCurrentUser(utilisateurRepository);
        return utilisateur.getPatientId();
    }

    /**
     * Récupère l'identifiant médecin de l'utilisateur authentifié.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @return l'identifiant médecin, ou null si l'utilisateur n'est pas un médecin
     * @throws IllegalStateException si l'utilisateur n'est pas authentifié
     */
    public static String getCurrentMedecinId(UtilisateurRepository utilisateurRepository) {
        Utilisateur utilisateur = getCurrentUser(utilisateurRepository);
        return utilisateur.getMedecinId();
    }

    /**
     * Vérifie si l'utilisateur authentifié est un administrateur.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @return true si l'utilisateur a le rôle ADMIN
     * @throws IllegalStateException si l'utilisateur n'est pas authentifié
     */
    public static boolean isAdmin(UtilisateurRepository utilisateurRepository) {
        return getCurrentRole(utilisateurRepository) == Role.ADMIN;
    }

    /**
     * Vérifie si l'utilisateur authentifié est un patient.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @return true si l'utilisateur a le rôle PATIENT
     * @throws IllegalStateException si l'utilisateur n'est pas authentifié
     */
    public static boolean isPatient(UtilisateurRepository utilisateurRepository) {
        return getCurrentRole(utilisateurRepository) == Role.PATIENT;
    }

    /**
     * Vérifie si l'utilisateur authentifié est un médecin.
     *
     * @param utilisateurRepository le repository des utilisateurs
     * @return true si l'utilisateur a le rôle MEDECIN
     * @throws IllegalStateException si l'utilisateur n'est pas authentifié
     */
    public static boolean isMedecin(UtilisateurRepository utilisateurRepository) {
        return getCurrentRole(utilisateurRepository) == Role.MEDECIN;
    }
}
