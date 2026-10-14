
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.LoginRequest;
import fr.dawan.cc_java_mongo.dto.LoginResponse;
import fr.dawan.cc_java_mongo.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour l'authentification des utilisateurs.
 * <p>
 * Ce contrôleur expose l'endpoint de connexion qui authentifie les utilisateurs
 * et renvoie un jeton JWT pour les requêtes ultérieures.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.security.JwtService
 * @see fr.dawan.cc_java_mongo.dto.LoginRequest
 * @see fr.dawan.cc_java_mongo.dto.LoginResponse
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Construit le contrôleur avec les composants d'authentification.
     *
     * @param authenticationManager le gestionnaire d'authentification Spring Security
     * @param jwtService le service de génération des jetons JWT
     */
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Authentifie un utilisateur et renvoie un jeton JWT.
     * <p>
     * Endpoint : {@code POST /api/auth/login}
     * </p>
     * <p>
     * Authentifie l'utilisateur avec ses identifiants, puis génère un jeton JWT
     * valide pour la durée configurée. Le jeton doit être inclus dans l'en-tête
     * {@code Authorization: Bearer} des requêtes ultérieures.
     * </p>
     *
     * @param request les identifiants de connexion (username et password)
     * @return la réponse contenant le jeton JWT, son type et sa durée de validité
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs());
    }
}

