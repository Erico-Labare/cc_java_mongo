
package fr.dawan.cc_java_mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente un utilisateur autorisé à se connecter à l'API.
 * <p>
 * Cette classe est mappée à la collection {@code utilisateurs} dans MongoDB.
 * Elle stocke les identifiants de connexion, le rôle de l'utilisateur
 * pour l'authentification JWT, et les références vers les profils métier
 * (Patient ou Médecin) associés.
 * </p>
 * <p>
 * Les associations avec les profils métier permettent de lier un compte
 * utilisateur à un patient ou un médecin spécifique, ce qui est nécessaire
 * pour les contrôles d'accès basés sur l'identité de l'utilisateur.
 * </p>
 *
 * @see org.springframework.data.mongodb.core.mapping.Document
 * @see fr.dawan.cc_java_mongo.security.JwtService
 * @see Role
 */
@Document(collection = "utilisateurs")
public class Utilisateur {

    @Id
    private String id;

    private String username;
    private String password;
    private Role role;

    /**
     * Référence vers le profil patient associé (numéro de sécurité sociale).
     * Ce champ est utilisé uniquement lorsque le rôle est {@link Role#PATIENT}.
     */
    private String patientId;

    /**
     * Référence vers le profil médecin associé (matricule).
     * Ce champ est utilisé uniquement lorsque le rôle est {@link Role#MEDECIN}.
     */
    private String medecinId;

    /**
     * Constructeur par défaut requis par Spring Data MongoDB.
     */
    public Utilisateur() {
    }

    /**
     * Construit un utilisateur avec ses informations complètes.
     *
     * @param id l'identifiant MongoDB de l'utilisateur
     * @param username le nom d'utilisateur pour la connexion
     * @param password le mot de passe chiffré
     * @param role le rôle de l'utilisateur
     */
    public Utilisateur(
            String id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Construit un utilisateur avec ses informations et une association patient.
     *
     * @param id l'identifiant MongoDB de l'utilisateur
     * @param username le nom d'utilisateur pour la connexion
     * @param password le mot de passe chiffré
     * @param role le rôle de l'utilisateur (doit être {@link Role#PATIENT})
     * @param patientId le numéro de sécurité sociale du patient associé
     */
    public Utilisateur(
            String id, String username, String password, Role role, String patientId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.patientId = patientId;
    }

    /**
     * Construit un utilisateur avec ses informations et une association médecin.
     *
     * @param id l'identifiant MongoDB de l'utilisateur
     * @param username le nom d'utilisateur pour la connexion
     * @param password le mot de passe chiffré
     * @param role le rôle de l'utilisateur (doit être {@link Role#MEDECIN})
     * @param medecinId le matricule du médecin associé
     */
    public Utilisateur(
            String id, String username, String password, Role role, String patientId, String medecinId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.patientId = patientId;
        this.medecinId = medecinId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(String medecinId) {
        this.medecinId = medecinId;
    }
}

