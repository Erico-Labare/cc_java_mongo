
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Tag(name = "Authentification", description = "Connexion et émission des jetons d'accès JWT.")
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
    @Operation(summary = "Se connecter", description = "Authentifie les identifiants fournis. Utilisez le jeton retourné dans l'en-tête Authorization: Bearer pour appeler les routes protégées.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie.", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corps absent, mal formé ou identifiants manquants."),
            @ApiResponse(responseCode = "401", description = "Nom d'utilisateur ou mot de passe invalide.")
    })
    public LoginResponse login(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest request) {

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

