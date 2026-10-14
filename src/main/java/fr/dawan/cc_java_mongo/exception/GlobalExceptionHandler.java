
package fr.dawan.cc_java_mongo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import java.time.Clock;

/**
 * Centralise le traitement des exceptions de l'API REST.
 * <p>
 * Cette classe intercepte les exceptions levées par l'application et les convertit
 * en réponses HTTP appropriées avec un format JSON cohérent. Elle gère notamment :
 * <ul>
 * <li>Les ressources non trouvées ({@link ResourceNotFoundException}) → 404</li>
 * <li>Les erreurs de validation ({@link MethodArgumentNotValidException}) → 400</li>
 * <li>Les erreurs d'authentification → 401</li>
 * <li>Les arguments invalides ({@link IllegalArgumentException}) → 400</li>
 * <li>Les erreurs de format de message → 400</li>
 * <li>Les exceptions inattendues → 500</li>
 * </ul>
 *
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock = Clock.systemDefaultZone();

    /**
     * Gère les exceptions de ressource non trouvée.
     *
     * @param exception l'exception levée
     * @return une réponse HTTP 404 avec le message d'erreur
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException exception) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage());
    }

    /**
     * Gère les exceptions d'argument invalide.
     *
     * @param exception l'exception levée
     * @return une réponse HTTP 400 avec le message d'erreur
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage());
    }

    /**
     * Gère les exceptions d'authentification.
     *
     * @param exception l'exception levée
     * @return une réponse HTTP 401 avec un message générique
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            Exception exception) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Identifiants invalides.");
    }


    /**
     * Gère les exceptions de validation des arguments de requête.
     * <p>
     * Extrait tous les erreurs de validation des champs et les retourne
     * dans une réponse structurée.
     * </p>
     *
     * @param exception l'exception de validation
     * @return une réponse HTTP 400 avec les détails des erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now(clock));
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation échouée");
        body.put("details", errors);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Gère les exceptions de lecture du corps de la requête.
     *
     * @param exception l'exception levée
     * @return une réponse HTTP 400 avec un message générique
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableMessage(
            HttpMessageNotReadableException exception) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Le corps de la requête est absent ou mal formé.");
    }

    /**
     * Gère toutes les exceptions non gérées spécifiquement.
     * <p>
     * Ce gestionnaire de dernier recours capture les exceptions inattendues
     * pour éviter de renvoyer des détails techniques au client.
     * </p>
     *
     * @param exception l'exception levée
     * @return une réponse HTTP 500 avec un message générique
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception exception) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue.");
    }

    /**
     * Construit une réponse d'erreur standardisée.
     *
     * @param status le statut HTTP de la réponse
     * @param message le message d'erreur
     * @return une ResponseEntity avec le corps de réponse structuré
     */
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now(clock));
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}

