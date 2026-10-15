
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.PatientRequest;
import fr.dawan.cc_java_mongo.dto.PatientResponse;
import fr.dawan.cc_java_mongo.model.Patient;
import fr.dawan.cc_java_mongo.service.PatientService;

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
 * Contrôleur REST pour la gestion des patients.
 * <p>
 * Ce contrôleur expose les endpoints CRUD pour les patients :
 * </p>
 * <ul>
 * <li>{@code GET /api/patients} - Liste des patients avec pagination et recherche</li>
 * <li>{@code GET /api/patients/{numeroSecuriteSociale}} - Détails d'un patient</li>
 * <li>{@code POST /api/patients} - Création d'un patient</li>
 * <li>{@code PUT /api/patients/{numeroSecuriteSociale}} - Modification d'un patient</li>
 * <li>{@code DELETE /api/patients/{numeroSecuriteSociale}} - Suppression d'un patient</li>
 * </ul>
 * <p>
 * Tous les endpoints nécessitent une authentification JWT valide.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.service.PatientService
 * @see fr.dawan.cc_java_mongo.dto.PatientRequest
 * @see fr.dawan.cc_java_mongo.dto.PatientResponse
 */
@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Consultation et gestion des dossiers patients.")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;

    /**
     * Construit le contrôleur avec le service des patients.
     *
     * @param patientService le service métier des patients
     */
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Récupère les patients avec pagination et recherche facultative.
     */
    @GetMapping
    @Operation(summary = "Lister les patients", description = "Retourne une page de patients, éventuellement filtrée par nom. Un patient ne voit que son dossier ; un médecin ne voit que les patients rattachés à lui ; un administrateur voit l'ensemble des dossiers.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Page de patients.", content = @Content(schema = @Schema(implementation = PatientResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Accès refusé par les règles métier.")})
    public Page<PatientResponse> getPatients(
            @Parameter(description = "Filtre facultatif : nom ou partie du nom.") @RequestParam(required = false) String nom,
            @ParameterObject @PageableDefault(size = 10, sort = "nom") Pageable pageable) {

        Page<Patient> patients = (nom == null || nom.isBlank())
                ? patientService.getAllPatients(pageable)
                : patientService.searchPatients(nom, pageable);

        return patients.map(this::toResponse);
    }

    /**
     * Récupère un patient par son numéro de Sécurité sociale.
     */
    @GetMapping("/{numeroSecuriteSociale}")
    @Operation(summary = "Consulter un patient", description = "Retourne le dossier identifié par son numéro de sécurité sociale. Un patient peut consulter son propre dossier ; un médecin uniquement un patient qui lui est rattaché ; un administrateur peut consulter tous les dossiers.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Dossier du patient.", content = @Content(schema = @Schema(implementation = PatientResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Le rôle ou le rattachement ne permet pas l'accès."), @ApiResponse(responseCode = "404", description = "Patient inexistant.")})
    public PatientResponse getPatient(
            @Parameter(description = "Numéro de sécurité sociale du patient.", example = "123456789012345") @PathVariable String numeroSecuriteSociale) {

        return toResponse(
                patientService.getPatientById(numeroSecuriteSociale));
    }

    /**
     * Crée un patient.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un patient", description = "Crée un dossier patient. Réservé aux administrateurs. Le numéro doit contenir exactement 15 chiffres ; le nom est obligatoire et limité à 100 caractères.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Dossier créé.", content = @Content(schema = @Schema(implementation = PatientResponse.class))), @ApiResponse(responseCode = "400", description = "Données invalides ou numéro déjà utilisé."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Réservé à un administrateur.")})
    public PatientResponse createPatient(
            @Valid @RequestBody PatientRequest request) {

        Patient patient = new Patient(
                request.getNumeroSecuriteSociale(),
                request.getNom());

        return toResponse(patientService.createPatient(patient));
    }

    /**
     * Modifie les données d'un patient existant.
     */
    @PutMapping("/{numeroSecuriteSociale}")
    @Operation(summary = "Modifier un patient", description = "Met à jour le nom du patient. Un administrateur peut modifier tout dossier ; un patient ne peut modifier que son propre dossier. Le DTO exige aussi un numéro de sécurité sociale valide dans le corps, mais le numéro du chemin identifie le dossier et la valeur du corps n'est pas utilisée pour le remplacer.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Dossier mis à jour.", content = @Content(schema = @Schema(implementation = PatientResponse.class))), @ApiResponse(responseCode = "400", description = "Corps invalide."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Modification non autorisée."), @ApiResponse(responseCode = "404", description = "Patient inexistant.")})
    public PatientResponse updatePatient(
            @Parameter(description = "Numéro de sécurité sociale du dossier.", example = "123456789012345") @PathVariable String numeroSecuriteSociale,
            @Valid @RequestBody PatientRequest request) {

        Patient patient = new Patient(
                numeroSecuriteSociale,
                request.getNom());

        return toResponse(
                patientService.updatePatient(numeroSecuriteSociale, patient));
    }

    /**
     * Supprime un patient.
     */
    @DeleteMapping("/{numeroSecuriteSociale}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un patient", description = "Supprime le dossier indiqué. Réservé aux administrateurs.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Dossier supprimé."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Réservé à un administrateur."), @ApiResponse(responseCode = "404", description = "Patient inexistant.")})
    public void deletePatient(
            @Parameter(description = "Numéro de sécurité sociale du patient.", example = "123456789012345") @PathVariable String numeroSecuriteSociale) {

        patientService.deletePatient(numeroSecuriteSociale);
    }

    /**
     * Convertit un modèle Patient en DTO de réponse.
     */
    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getNumeroSecuriteSociale(),
                patient.getNom());
    }
}
