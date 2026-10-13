
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Données reçues pour créer ou modifier une consultation.
 */
public class ConsultationRequest {

    @NotBlank(message = "Le numéro de consultation est obligatoire.")
    @Size(max = 50, message = "Le numéro ne doit pas dépasser 50 caractères.")
    private String numero;

    @NotNull(message = "La date de consultation est obligatoire.")
    @Future(message = "La consultation doit être programmée dans le futur.")
    private LocalDateTime date;

    @NotBlank(message = "L'identifiant du patient est obligatoire.")
    private String patientId;

    @NotBlank(message = "Le matricule du médecin est obligatoire.")
    private String medecinId;

    @Valid
    private List<PrescriptionRequest> prescriptions = new ArrayList<>();

    public ConsultationRequest() {
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(String medecinId) {
        this.medecinId = medecinId;
    }

    public List<PrescriptionRequest> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<PrescriptionRequest> prescriptions) {
        this.prescriptions = prescriptions;
    }
}
