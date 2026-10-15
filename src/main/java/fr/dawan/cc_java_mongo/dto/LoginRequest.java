
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données de connexion d'un utilisateur.
 * <p>
 * Cette classe contient les identifiants (nom d'utilisateur et mot de passe)
 * nécessaires pour authentifier un utilisateur et obtenir un jeton JWT.
 * </p>
 */
@Schema(name = "LoginRequest", description = "Identifiants transmis à la route publique de connexion.")
public class LoginRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Schema(description = "Nom d'utilisateur du compte.", example = "medecin.dupont", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Schema(description = "Mot de passe en clair transmis en HTTPS ; il n'est jamais renvoyé par l'API.", example = "MotDePasseExemple!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
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
