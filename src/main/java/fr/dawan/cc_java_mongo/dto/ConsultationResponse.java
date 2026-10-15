
package fr.dawan.cc_java_mongo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données de réponse d'une consultation.
 * <p>
 * Cette classe contient les données renvoyées par l'API après une opération
 * sur une consultation (création, lecture, modification).
 * </p>
 */
@Schema(name = "ConsultationResponse", description = "Consultation telle que renvoyée par l'API, avec ses références et prescriptions.")
public class ConsultationResponse {

    @Schema(description = "Numéro unique de consultation.", example = "CONS-2026-001")
    private String numero;
    @Schema(description = "Date et heure de la consultation.", example = "2026-11-20T14:30:00", type = "string", format = "date-time")
    private LocalDateTime date;
    @Schema(description = "Numéro de sécurité sociale du patient.", example = "123456789012345")
    private String patientId;
    @Schema(description = "Matricule du médecin.", example = "MED-1042")
    private String medecinId;
    @Schema(description = "Prescriptions rattachées à la consultation.")
    private List<PrescriptionRequest> prescriptions = new ArrayList<>();
    @Schema(description = "Identifiant GridFS du document associé, nul si aucun document n'est joint.", example = "507f1f77bcf86cd799439011", nullable = true)
    private String documentId;

    public ConsultationResponse() {
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

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
