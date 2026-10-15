
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.MedecinRequest;
import fr.dawan.cc_java_mongo.dto.MedecinResponse;
import fr.dawan.cc_java_mongo.model.Medecin;
import fr.dawan.cc_java_mongo.service.MedecinService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;

/**
 * Contrôleur REST pour la gestion des médecins.
 * <p>
 * Ce contrôleur expose les endpoints CRUD pour les médecins :
 * </p>
 * <ul>
 * <li>{@code GET /api/medecins} - Liste des médecins avec pagination et recherche</li>
 * <li>{@code GET /api/medecins/{matricule}} - Détails d'un médecin</li>
 * <li>{@code POST /api/medecins} - Création d'un médecin</li>
 * <li>{@code PUT /api/medecins/{matricule}} - Modification d'un médecin</li>
 * <li>{@code DELETE /api/medecins/{matricule}} - Suppression d'un médecin</li>
 * </ul>
 * <p>
 * Tous les endpoints nécessitent une authentification JWT valide.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.service.MedecinService
 * @see fr.dawan.cc_java_mongo.dto.MedecinRequest
 * @see fr.dawan.cc_java_mongo.dto.MedecinResponse
 */
@RestController
@RequestMapping("/api/medecins")
@Tag(name = "Médecins", description = "Recherche et gestion des profils médecins.")
@SecurityRequirement(name = "bearerAuth")
public class MedecinController {

    private final MedecinService medecinService;

    /**
     * Construit le contrôleur avec le service des médecins.
     *
     * @param medecinService le service métier des médecins
     */
    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }

    /**
     * Récupère les médecins avec pagination et recherche facultative.
     */
    @GetMapping
    @Operation(summary = "Lister ou rechercher les médecins", description = "Retourne une page triée par nom (10 éléments par défaut). Le filtre nom est facultatif et recherche une correspondance partielle sans tenir compte de la casse. Un patient n'a pas accès ; les rôles médecin et administrateur peuvent consulter.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Page de médecins.", content = @Content(schema = @Schema(implementation = MedecinResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Les patients ne peuvent pas consulter les profils médecins.")})
    public Page<MedecinResponse> getMedecins(
            @Parameter(description = "Nom ou partie du nom à rechercher.") @RequestParam(required = false) String nom,
            @ParameterObject @PageableDefault(size = 10, sort = "nom") Pageable pageable) {

        Page<Medecin> medecins = (nom == null || nom.isBlank())
                ? medecinService.getAllMedecins(pageable)
                : medecinService.searchMedecins(nom, pageable);

        return medecins.map(this::toResponse);
    }

    /**
     * Récupère un médecin par son matricule.
     */
    @GetMapping("/{matricule}")
    @Operation(summary = "Consulter un médecin", description = "Retourne le profil associé au matricule. Accessible aux rôles médecin et administrateur.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Profil médecin.", content = @Content(schema = @Schema(implementation = MedecinResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Les patients ne peuvent pas consulter les profils médecins."), @ApiResponse(responseCode = "404", description = "Médecin inexistant.")})
    public MedecinResponse getMedecin(@Parameter(description = "Matricule du médecin.", example = "MED-1042") @PathVariable String matricule) {
        return toResponse(medecinService.getMedecinById(matricule));
    }

    /**
     * Crée un médecin.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un médecin", description = "Crée un profil médecin. Réservé aux administrateurs ; le matricule (50 caractères maximum) et le nom (100 caractères maximum) sont obligatoires.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Profil créé.", content = @Content(schema = @Schema(implementation = MedecinResponse.class))), @ApiResponse(responseCode = "400", description = "Données invalides ou matricule déjà utilisé."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Réservé à un administrateur.")})
    public MedecinResponse createMedecin(
            @Valid @RequestBody MedecinRequest request) {

        Medecin medecin = new Medecin(
                request.getMatricule(),
                request.getNom());

        return toResponse(medecinService.createMedecin(medecin));
    }

    /**
     * Modifie un médecin existant.
     */
    @PutMapping("/{matricule}")
    @Operation(summary = "Modifier un médecin", description = "Met à jour le nom du profil indiqué. Réservé aux administrateurs.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Profil mis à jour.", content = @Content(schema = @Schema(implementation = MedecinResponse.class))), @ApiResponse(responseCode = "400", description = "Corps invalide."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Réservé à un administrateur."), @ApiResponse(responseCode = "404", description = "Médecin inexistant.")})
    public MedecinResponse updateMedecin(
            @Parameter(description = "Matricule du profil à modifier.", example = "MED-1042") @PathVariable String matricule,
            @Valid @RequestBody MedecinRequest request) {

        Medecin medecin = new Medecin(matricule, request.getNom());

        return toResponse(
                medecinService.updateMedecin(matricule, medecin));
    }

    /**
     * Supprime un médecin.
     */
    @DeleteMapping("/{matricule}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un médecin", description = "Supprime le profil indiqué. Réservé aux administrateurs.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Profil supprimé."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Réservé à un administrateur."), @ApiResponse(responseCode = "404", description = "Médecin inexistant.")})
    public void deleteMedecin(@Parameter(description = "Matricule du médecin.", example = "MED-1042") @PathVariable String matricule) {
        medecinService.deleteMedecin(matricule);
    }

    /**
     * Convertit un modèle Medecin en DTO de réponse.
     */
    private MedecinResponse toResponse(Medecin medecin) {
        return new MedecinResponse(
                medecin.getMatricule(),
                medecin.getNom());
    }
}
