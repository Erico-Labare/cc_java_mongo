
package fr.dawan.cc_java_mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente un utilisateur autorisé à se connecter à l'API.
 * <p>
 * Cette classe est mappée à la collection {@code utilisateurs} dans MongoDB.
 * Elle stocke les identifiants de connexion et le rôle de l'utilisateur
 * pour l'authentification JWT.
 * </p>
 *
 * @see org.springframework.data.mongodb.core.mapping.Document
 * @see fr.dawan.cc_java_mongo.security.JwtService
 */
@Document(collection = "utilisateurs")
public class Utilisateur {

    @Id
    private String id;

    private String username;
    private String password;
    private String role;

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
     * @param role le rôle de l'utilisateur (ex: {@code USER}, {@code ADMIN})
     */
    public Utilisateur(
            String id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

