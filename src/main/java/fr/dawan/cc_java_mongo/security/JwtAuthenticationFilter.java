
package fr.dawan.cc_java_mongo.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre Spring Security pour l'authentification par JWT.
 * <p>
 * Ce filtre intercepte chaque requête HTTP pour extraire et valider le jeton JWT
 * présent dans l'en-tête {@code Authorization: Bearer}. Si le jeton est valide,
 * il authentifie l'utilisateur dans le contexte de sécurité Spring.
 * </p>
 * <p>
 * Le filtre ne s'exécute qu'une fois par requête grâce à l'héritage de
 * {@link OncePerRequestFilter}.
 * </p>
 *
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see fr.dawan.cc_java_mongo.security.JwtService
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Construit le filtre d'authentification JWT.
     *
     * @param jwtService le service de génération et vérification des jetons
     * @param userDetailsService le service de chargement des utilisateurs
     */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Traite la requête HTTP pour authentifier l'utilisateur via JWT.
     * <p>
     * Extrait le jeton de l'en-tête Authorization, vérifie sa validité,
     * charge l'utilisateur correspondant et l'authentifie dans le contexte
     * de sécurité Spring. Si le jeton est absent ou invalide, la requête
     * continue sans authentification.
     * </p>
     *
     * @param request la requête HTTP
     * @param response la réponse HTTP
     * @param filterChain la chaîne de filtres Spring Security
     * @throws ServletException en cas d'erreur de servlet
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);

            if (username != null
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        } catch (JwtException | IllegalArgumentException exception) {
            // Le jeton est invalide : la requête reste non authentifiée.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

