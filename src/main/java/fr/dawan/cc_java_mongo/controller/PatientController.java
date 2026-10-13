
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

/**
 * Expose les endpoints REST de gestion des patients.
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Récupère les patients avec pagination et recherche facultative.
     */
    @GetMapping
    public Page<PatientResponse> getPatients(
            @RequestParam(required = false) String nom,
            @PageableDefault(size = 10, sort = "nom") Pageable pageable) {

        Page<Patient> patients = (nom == null || nom.isBlank())
                ? patientService.getAllPatients(pageable)
                : patientService.searchPatients(nom, pageable);

        return patients.map(this::toResponse);
    }

    /**
     * Récupère un patient par son numéro de Sécurité sociale.
     */
    @GetMapping("/{numeroSecuriteSociale}")
    public PatientResponse getPatient(
            @PathVariable String numeroSecuriteSociale) {

        return toResponse(
                patientService.getPatientById(numeroSecuriteSociale));
    }

    /**
     * Crée un patient.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    public PatientResponse updatePatient(
            @PathVariable String numeroSecuriteSociale,
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
    public void deletePatient(
            @PathVariable String numeroSecuriteSociale) {

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
