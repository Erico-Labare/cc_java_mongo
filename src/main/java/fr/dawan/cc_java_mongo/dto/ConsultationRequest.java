
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO pour les données de création ou modification d'une consultation.
 * <p>
 * Cette classe contient les données reçues du client pour créer ou modifier
 * une consultation. Les champs sont validés par les annotations Jakarta Validation.
 * La date de la consultation doit être dans le futur.
 * </p>
 */
@Schema(name = "ConsultationRequest", description = "Données de création ou de mise à jour d'une consultation et de ses prescriptions.")
public class ConsultationRequest {

    @NotBlank(message = "Le numéro de consultation est obligatoire.")
    @Size(max = 50, message = "Le numéro ne doit pas dépasser 50 caractères.")
    @Schema(description = "Numéro unique de consultation (50 caractères maximum).", example = "CONS-2026-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numero;

    @NotNull(message = "La date de consultation est obligatoire.")
    @Future(message = "La consultation doit être programmée dans le futur.")
    @Schema(description = "Date et heure futures de la consultation, au format ISO-8601.", example = "2026-11-20T14:30:00", type = "string", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime date;

    @NotBlank(message = "L'identifiant du patient est obligatoire.")
    @Schema(description = "Numéro de sécurité sociale d'un patient existant.", example = "123456789012345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String patientId;

    @NotBlank(message = "Le matricule du médecin est obligatoire.")
    @Schema(description = "Matricule d'un médecin existant.", example = "MED-1042", requiredMode = Schema.RequiredMode.REQUIRED)
    private String medecinId;

    @Valid
    @Schema(description = "Prescriptions initiales ; facultatif, liste vide par défaut.")
    private List<PrescriptionRequest> prescriptions = new ArrayList<>();

    public ConsultationRequest() {
        // Default constructor required for JSON deserialization
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
