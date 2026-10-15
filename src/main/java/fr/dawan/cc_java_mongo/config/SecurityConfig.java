
package fr.dawan.cc_java_mongo.config;

import fr.dawan.cc_java_mongo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration Spring Security pour l'authentification et l'autorisation.
 * <p>
 * Cette configuration définit les règles de sécurité de l'API REST :
 * <ul>
 * <li>Authentification basée sur JWT via {@link fr.dawan.cc_java_mongo.security.JwtAuthenticationFilter}</li>
 * <li>Protection CSRF désactivée (API stateless)</li>
 * <li>Sessions HTTP désactivées (authentification par token uniquement)</li>
 * <li>Endpoints publics : authentification, health check, documentation Swagger</li>
 * <li>Endpoints d'administration réservés au rôle ADMIN</li>
 * <li>Tous les autres endpoints nécessitent une authentification JWT valide</li>
 * </ul>
 * <p>
 * <strong>Note importante :</strong> Les contrôles d'accès basés sur l'identité
 * de l'utilisateur (ex: un patient ne peut voir que son propre dossier) sont
 * implémentés au niveau des services métier, pas uniquement dans cette configuration.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.security.JwtAuthenticationFilter
 * @see fr.dawan.cc_java_mongo.security.JwtService
 */
@Configuration
public class SecurityConfig {

    /**
     * Configure le fournisseur d'authentification.
     * <p>
     * Utilise un {@link DaoAuthenticationProvider} qui charge les utilisateurs
     * via le {@link UserDetailsService} et vérifie les mots de passe avec le
     * {@link PasswordEncoder} configuré.
     * </p>
     *
     * @param userDetailsService service de chargement des utilisateurs
     * @param passwordEncoder encodeur de mot de passe
     * @return le fournisseur d'authentification configuré
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Expose le gestionnaire d'authentification Spring Security.
     *
     * @param configuration la configuration d'authentification Spring
     * @return le gestionnaire d'authentification
     * @throws Exception si la configuration échoue
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Configure la chaîne de filtres de sécurité HTTP.
     * <p>
     * Configure CORS, désactive CSRF, utilise une politique de session stateless,
     * définit les règles d'autorisation des requêtes et ajoute le filtre JWT
     * avant le filtre d'authentification par nom d'utilisateur/mot de passe.
     * </p>
     * <p>
     * Les endpoints d'administration (gestion des utilisateurs) sont réservés
     * au rôle ADMIN. Les autres endpoints métier nécessitent une authentification
     * mais les contrôles d'accès basés sur l'identité sont effectués dans les services.
     * </p>
     *
     * @param http la configuration HTTP de Spring Security
     * @param jwtAuthenticationFilter le filtre d'authentification JWT
     * @param authenticationProvider le fournisseur d'authentification
     * @return la chaîne de filtres de sécurité configurée
     * @throws Exception si la configuration échoue
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider) throws Exception {

        http
                .cors(cors -> {
                })
                // CSRF est désactivé car cette API est stateless :
                // l'authentification repose exclusivement sur un JWT transmis
                // explicitement dans l'en-tête Authorization : Bearer.
                // Aucun cookie ni session HTTP n'est utilisé pour authentifier
                // les requêtes. Cette configuration doit être réévaluée si
                // l'authentification par cookie est introduite.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/**",
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Les endpoints d'administration sont réservés au rôle ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Tous les autres endpoints nécessitent une authentification
                        // Les contrôles d'accès basés sur l'identité sont dans les services
                        .anyRequest().authenticated())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

