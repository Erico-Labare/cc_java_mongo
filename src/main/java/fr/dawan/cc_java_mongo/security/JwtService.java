package fr.dawan.cc_java_mongo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/**
 * Service de génération et de vérification des jetons JWT.
 * <p>
 * Ce service utilise la bibliothèque JJWT pour créer et valider des jetons JWT
 * signés avec une clé secrète configurée. Les jetons contiennent le nom d'utilisateur
 * comme sujet et expirent après une durée configurée.
 * </p>
 *
 * @see io.jsonwebtoken.Jwts
 * @see org.springframework.security.core.userdetails.UserDetails
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    /**
     * Construit le service JWT avec la clé secrète et la durée d'expiration.
     *
     * @param secret la clé secrète encodée en BASE64 (configurée via {@code app.jwt.secret})
     * @param expirationMs la durée de validité du jeton en millisecondes
     * @throws IllegalStateException si la clé secrète n'est pas configurée
     */
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "La variable d'environnement JWT_SECRET doit être configurée.");
        }

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Génère un jeton JWT pour l'utilisateur indiqué.
     *
     * @param userDetails les détails de l'utilisateur authentifié
     * @return le jeton JWT signé
     */
    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur du jeton JWT.
     *
     * @param token le jeton JWT à analyser
     * @return le nom d'utilisateur contenu dans le jeton
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Vérifie la validité du jeton et son association à l'utilisateur.
     * <p>
     * Le jeton est valide s'il n'est pas expiré et si le sujet correspond
     * au nom d'utilisateur fourni.
     * </p>
     *
     * @param token le jeton JWT à vérifier
     * @param userDetails les détails de l'utilisateur à comparer
     * @return {@code true} si le jeton est valide, {@code false} sinon
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);

            return claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().toInstant().isAfter(Instant.now());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Retourne la durée d'expiration configurée, en millisecondes.
     *
     * @return la durée de validité des jetons en millisecondes
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Extrait toutes les claims (revendications) du jeton JWT.
     *
     * @param token le jeton JWT à analyser
     * @return les claims contenues dans le jeton
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

