
package fr.dawan.cc_java_mongo.dto;

/**
 * DTO pour la réponse d'authentification.
 * <p>
 * Cette classe contient les données renvoyées après une authentification réussie :
 * le jeton JWT, son type et sa durée de validité.
 * </p>
 */
public class LoginResponse {

    private String token;
    private String tokenType;
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

