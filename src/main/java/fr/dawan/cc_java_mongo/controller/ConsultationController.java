
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
    public Page<ConsultationResponse> getConsultations(
            @PageableDefault(size = 10, sort = "date") Pageable pageable) {

        return consultationService.getAllConsultations(pageable)
                .map(this::toResponse);
    }

    /**
     * Récupère les consultations d'un patient avec pagination.
     */
    @GetMapping("/patient/{patientId}")
    public Page<ConsultationResponse> getConsultationsByPatient(
            @PathVariable String patientId,
            @PageableDefault(size = 10, sort = "date") Pageable pageable) {

        return consultationService.getConsultationsByPatient(
                        patientId, pageable)
                .map(this::toResponse);
    }

    /**
     * Récupère une consultation par son numéro.
     */
    @GetMapping("/{numero}")
    public ConsultationResponse getConsultation(@PathVariable String numero) {
        return toResponse(
                consultationService.getConsultationById(numero));
    }

    /**
     * Crée une consultation future.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    public ConsultationResponse updateConsultation(
            @PathVariable String numero,
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
    public void deleteConsultation(@PathVariable String numero) {
        consultationService.deleteConsultation(numero);
    }

    /**
     * Modifie les prescriptions d'une consultation.
     */
    @PutMapping("/{numero}/prescriptions")
    public ConsultationResponse updatePrescriptions(
            @PathVariable String numero,
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

