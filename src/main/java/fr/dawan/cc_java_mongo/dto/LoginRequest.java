
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour les données de connexion d'un utilisateur.
 * <p>
 * Cette classe contient les identifiants (nom d'utilisateur et mot de passe)
 * nécessaires pour authentifier un utilisateur et obtenir un jeton JWT.
 * </p>
 */
public class LoginRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    public LoginRequest() {
        // Default constructor required for JSON deserialization
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
}
