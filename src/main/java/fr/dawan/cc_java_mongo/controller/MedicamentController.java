
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.MedicamentRequest;
import fr.dawan.cc_java_mongo.dto.MedicamentResponse;
import fr.dawan.cc_java_mongo.model.Medicament;
import fr.dawan.cc_java_mongo.service.MedicamentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Contrôleur REST pour la gestion des médicaments.
 * <p>
 * Ce contrôleur expose les endpoints CRUD pour les médicaments :
 * </p>
 * <ul>
 * <li>{@code GET /api/medicaments} - Liste de tous les médicaments</li>
 * <li>{@code GET /api/medicaments/{code}} - Détails d'un médicament</li>
 * <li>{@code POST /api/medicaments} - Création d'un médicament</li>
 * <li>{@code PUT /api/medicaments/{code}} - Modification d'un médicament</li>
 * <li>{@code DELETE /api/medicaments/{code}} - Suppression d'un médicament</li>
 * </ul>
 * <p>
 * Tous les endpoints nécessitent une authentification JWT valide.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.service.MedicamentService
 * @see fr.dawan.cc_java_mongo.dto.MedicamentRequest
 * @see fr.dawan.cc_java_mongo.dto.MedicamentResponse
 */
@RestController
@RequestMapping("/api/medicaments")
@Tag(name = "Médicaments", description = "Consultation du catalogue et administration des médicaments.")
@SecurityRequirement(name = "bearerAuth")
public class MedicamentController {

    private final MedicamentService medicamentService;

    /**
     * Construit le contrôleur avec le service des médicaments.
     *
     * @param medicamentService le service métier des médicaments
     */
    public MedicamentController(MedicamentService medicamentService) {
        this.medicamentService = medicamentService;
    }

    /**
     * Récupère la liste des médicaments.
     */
    @GetMapping
    @Operation(summary = "Lister les médicaments", description = "Retourne la liste complète du catalogue. La route est accessible à tout utilisateur authentifié.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Catalogue des médicaments.", content = @Content(schema = @Schema(implementation = MedicamentResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide.")})
    public List<MedicamentResponse> getMedicaments() {
        return medicamentService.getAllMedicaments()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Récupère un médicament par son code.
     */
    @GetMapping("/{code}")
    @Operation(summary = "Consulter un médicament", description = "Retourne le médicament associé au code fourni.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Médicament trouvé.", content = @Content(schema = @Schema(implementation = MedicamentResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "404", description = "Médicament inexistant.")})
    public MedicamentResponse getMedicament(@Parameter(description = "Code unique du médicament.", example = "MED-001") @PathVariable String code) {
        return toResponse(medicamentService.getMedicamentById(code));
    }

    /**
     * Crée un médicament.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter un médicament", description = "Ajoute un médicament au catalogue. Réservé aux administrateurs ; le code (50 caractères maximum) et le libellé (150 caractères maximum) sont obligatoires.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Médicament créé.", content = @Content(schema = @Schema(implementation = MedicamentResponse.class))), @ApiResponse(responseCode = "400", description = "Données invalides ou code déjà utilisé."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Réservé à un administrateur.")})
    public MedicamentResponse createMedicament(
            @Valid @RequestBody MedicamentRequest request) {

        Medicament medicament = new Medicament(
                request.getCode(),
                request.getLibelle());

        return toResponse(medicamentService.createMedicament(medicament));
    }

    /**
     * Modifie un médicament existant.
     */
    @PutMapping("/{code}")
    @Operation(summary = "Modifier un médicament", description = "Met à jour le libellé du médicament identifié par le code du chemin. Réservé aux administrateurs.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Médicament mis à jour.", content = @Content(schema = @Schema(implementation = MedicamentResponse.class))), @ApiResponse(responseCode = "400", description = "Corps invalide."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Réservé à un administrateur."), @ApiResponse(responseCode = "404", description = "Médicament inexistant.")})
    public MedicamentResponse updateMedicament(
            @Parameter(description = "Code du médicament à modifier.", example = "MED-001") @PathVariable String code,
            @Valid @RequestBody MedicamentRequest request) {

        Medicament medicament = new Medicament(
                code,
                request.getLibelle());

        return toResponse(
                medicamentService.updateMedicament(code, medicament));
    }

    /**
     * Supprime un médicament.
     */
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un médicament", description = "Supprime du catalogue le médicament indiqué. Réservé aux administrateurs.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Médicament supprimé."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Réservé à un administrateur."), @ApiResponse(responseCode = "404", description = "Médicament inexistant.")})
    public void deleteMedicament(@Parameter(description = "Code du médicament.", example = "MED-001") @PathVariable String code) {
        medicamentService.deleteMedicament(code);
    }

    /**
     * Convertit un modèle Medicament en DTO de réponse.
     */
    private MedicamentResponse toResponse(Medicament medicament) {
        return new MedicamentResponse(
                medicament.getCode(),
                medicament.getLibelle());
    }
}

