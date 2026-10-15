
package fr.dawan.cc_java_mongo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour la réponse d'authentification.
 * <p>
 * Cette classe contient les données renvoyées après une authentification réussie :
 * le jeton JWT, son type et sa durée de validité.
 * </p>
 */
@Schema(name = "LoginResponse", description = "Jeton d'accès émis après une authentification réussie.")
public class LoginResponse {

    @Schema(description = "Jeton JWT à transmettre dans Authorization: Bearer.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZWRlY2luLmR1cG9udCJ9.signature")
    private String token;
    @Schema(description = "Type de jeton d'accès.", example = "Bearer")
    private String tokenType;
    @Schema(description = "Durée de validité configurée, en millisecondes.", example = "3600000")
    private long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, String tokenType, long expiresIn) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}

