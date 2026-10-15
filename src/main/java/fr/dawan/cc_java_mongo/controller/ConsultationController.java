
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.ConsultationRequest;
import fr.dawan.cc_java_mongo.dto.ConsultationResponse;
import fr.dawan.cc_java_mongo.dto.PrescriptionRequest;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Prescription;
import fr.dawan.cc_java_mongo.service.ConsultationService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
 * Contrôleur REST pour la gestion des consultations.
 * <p>
 * Ce contrôleur expose les endpoints CRUD pour les consultations :
 * </p>
 * <ul>
 * <li>{@code GET /api/consultations} - Liste des consultations avec pagination</li>
 * <li>{@code GET /api/consultations/patient/{patientId}} - Consultations d'un patient</li>
 * <li>{@code GET /api/consultations/{numero}} - Détails d'une consultation</li>
 * <li>{@code POST /api/consultations} - Création d'une consultation future</li>
 * <li>{@code PUT /api/consultations/{numero}} - Modification d'une consultation future</li>
 * <li>{@code DELETE /api/consultations/{numero}} - Suppression d'une consultation future</li>
 * <li>{@code PUT /api/consultations/{numero}/prescriptions} - Modification des prescriptions</li>
 * </ul>
 * <p>
 * Les opérations de modification et suppression sont restreintes aux consultations futures.
 * Tous les endpoints nécessitent une authentification JWT valide.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.service.ConsultationService
 * @see fr.dawan.cc_java_mongo.dto.ConsultationRequest
 * @see fr.dawan.cc_java_mongo.dto.ConsultationResponse
 */
@RestController
@RequestMapping("/api/consultations")
@Tag(name = "Consultations", description = "Consultation, planification et prescriptions médicales.")
@SecurityRequirement(name = "bearerAuth")
public class ConsultationController {

    private final ConsultationService consultationService;

    /**
     * Construit le contrôleur avec le service des consultations.
     *
     * @param consultationService le service métier des consultations
     */
    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    /**
     * Récupère toutes les consultations avec pagination.
     */
    @GetMapping
    @Operation(summary = "Lister les consultations", description = "Retourne les consultations paginées (10 éléments par défaut), triées par date. Les résultats sont limités aux consultations du patient connecté ou du médecin associé ; un administrateur voit toutes les consultations.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Page de consultations.", content = @Content(schema = @Schema(implementation = ConsultationResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide.")})
    public Page<ConsultationResponse> getConsultations(
            @ParameterObject @PageableDefault(size = 10, sort = "date") Pageable pageable) {

        return consultationService.getAllConsultations(pageable)
                .map(this::toResponse);
    }

    /**
     * Récupère les consultations d'un patient avec pagination.
     */
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Lister les consultations d'un patient", description = "Retourne une page des consultations du patient. Le patient ne peut interroger que son propre identifiant ; un médecin doit être rattaché à au moins une consultation du patient et ne reçoit que ses consultations ; l'administrateur voit toutes les consultations.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Page des consultations du patient.", content = @Content(schema = @Schema(implementation = ConsultationResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Accès au patient non autorisé."), @ApiResponse(responseCode = "404", description = "Patient inexistant.")})
    public Page<ConsultationResponse> getConsultationsByPatient(
            @Parameter(description = "Numéro de sécurité sociale du patient.", example = "123456789012345") @PathVariable String patientId,
            @ParameterObject @PageableDefault(size = 10, sort = "date") Pageable pageable) {

        return consultationService.getConsultationsByPatient(
                        patientId, pageable)
                .map(this::toResponse);
    }

    /**
     * Récupère une consultation par son numéro.
     */
    @GetMapping("/{numero}")
    @Operation(summary = "Consulter une consultation", description = "Retourne la consultation, ses références patient et médecin, ses prescriptions et l'identifiant du document associé. Patients et médecins sont limités à leurs consultations.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Consultation trouvée.", content = @Content(schema = @Schema(implementation = ConsultationResponse.class))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Consultation hors du périmètre de l'utilisateur."), @ApiResponse(responseCode = "404", description = "Consultation inexistante.")})
    public ConsultationResponse getConsultation(@Parameter(description = "Numéro de consultation.", example = "CONS-2026-001") @PathVariable String numero) {
        return toResponse(
                consultationService.getConsultationById(numero));
    }

    /**
     * Crée une consultation future.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une consultation", description = "Planifie une consultation future. Le patient ne peut pas la créer ; un médecin ne peut créer que pour lui-même et un patient qui lui est rattaché ; l'administrateur peut créer pour tout profil. La date doit être future et les références patient, médecin et médicaments doivent exister.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Consultation créée.", content = @Content(schema = @Schema(implementation = ConsultationResponse.class))), @ApiResponse(responseCode = "400", description = "Données invalides, date non future ou numéro déjà utilisé."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Création non autorisée pour ce rôle ou ce rattachement."), @ApiResponse(responseCode = "404", description = "Patient, médecin ou médicament référencé inexistant.")})
    public ConsultationResponse createConsultation(
            @Valid @RequestBody ConsultationRequest request) {

        Consultation consultation = toModel(request);
        return toResponse(
                consultationService.createConsultation(consultation));
    }

    /**
     * Modifie une consultation future.
     */
    @PutMapping("/{numero}")
    @Operation(summary = "Modifier une consultation", description = "Remplace la date, le patient, le médecin et les prescriptions d'une consultation future. Le numéro et le document associé sont conservés. Les patients ne peuvent pas modifier ; un médecin ne peut pas réaffecter les profils.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Consultation mise à jour.", content = @Content(schema = @Schema(implementation = ConsultationResponse.class))), @ApiResponse(responseCode = "400", description = "Données invalides ou consultation passée."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Modification ou réaffectation non autorisée."), @ApiResponse(responseCode = "404", description = "Consultation ou référence inexistante.")})
    public ConsultationResponse updateConsultation(
            @Parameter(description = "Numéro de la consultation à modifier.", example = "CONS-2026-001") @PathVariable String numero,
            @Valid @RequestBody ConsultationRequest request) {

        Consultation consultation = toModel(request);
        return toResponse(
                consultationService.updateConsultation(numero, consultation));
    }

    /**
     * Supprime une consultation future.
     */
    @DeleteMapping("/{numero}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une consultation", description = "Supprime une consultation future. Les patients ne peuvent pas la supprimer ; les autres rôles doivent avoir accès à la consultation.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Consultation supprimée."), @ApiResponse(responseCode = "400", description = "La consultation n'est pas future."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Suppression non autorisée."), @ApiResponse(responseCode = "404", description = "Consultation inexistante.")})
    public void deleteConsultation(@Parameter(description = "Numéro de la consultation à supprimer.", example = "CONS-2026-001") @PathVariable String numero) {
        consultationService.deleteConsultation(numero);
    }

    /**
     * Modifie les prescriptions d'une consultation.
     */
    @PutMapping("/{numero}/prescriptions")
    @Operation(summary = "Remplacer les prescriptions", description = "Remplace la liste complète des prescriptions d'une consultation future. Envoyer un tableau vide pour retirer toutes les prescriptions. Chaque médicament doit exister et le nombre de prises être strictement positif. Les patients ne peuvent pas effectuer cette opération.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Prescriptions remplacées.", content = @Content(schema = @Schema(implementation = ConsultationResponse.class))), @ApiResponse(responseCode = "400", description = "Requête invalide, nombre de prises non positif ou consultation passée."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Modification non autorisée."), @ApiResponse(responseCode = "404", description = "Consultation ou médicament inexistant.")})
    public ConsultationResponse updatePrescriptions(
            @Parameter(description = "Numéro de la consultation.", example = "CONS-2026-001") @PathVariable String numero,
            @Valid @RequestBody List<@Valid PrescriptionRequest> prescriptions) {

        List<Prescription> modelPrescriptions = prescriptions == null
                ? new ArrayList<>()
                : prescriptions.stream()
                .map(this::toModel)
                .toList();

        return toResponse(
                consultationService.updatePrescriptions(
                        numero, modelPrescriptions));
    }

    /**
     * Convertit une requête en modèle Consultation.
     */
    private Consultation toModel(ConsultationRequest request) {
        Consultation consultation = new Consultation(
                request.getNumero(),
                request.getDate(),
                request.getPatientId(),
                request.getMedecinId());

        if (request.getPrescriptions() != null) {
            consultation.setPrescriptions(
                    request.getPrescriptions().stream()
                            .map(this::toModel)
                            .toList());
        }

        return consultation;
    }

    /**
     * Convertit une requête en modèle Prescription.
     */
    private Prescription toModel(PrescriptionRequest request) {
        return new Prescription(
                request.getMedicamentId(),
                request.getNbPrises());
    }

    /**
     * Convertit un modèle Consultation en DTO de réponse.
     */
    private ConsultationResponse toResponse(Consultation consultation) {
        ConsultationResponse response = new ConsultationResponse();

        response.setNumero(consultation.getNumero());
        response.setDate(consultation.getDate());
        response.setPatientId(consultation.getPatientId());
        response.setMedecinId(consultation.getMedecinId());
        response.setDocumentId(consultation.getDocumentId());

        if (consultation.getPrescriptions() != null) {
            response.setPrescriptions(
                    consultation.getPrescriptions().stream()
                            .map(p -> {
                                PrescriptionRequest dto =
                                        new PrescriptionRequest();
                                dto.setMedicamentId(p.getMedicamentId());
                                dto.setNbPrises(p.getNbPrises());
                                return dto;
                            })
                            .toList());
        }

        return response;
    }
}

