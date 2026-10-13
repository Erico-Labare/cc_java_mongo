
package fr.dawan.cc_java_mongo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente une consultation médicale enregistrée dans MongoDB.
 */
@Document(collection = "consultations")
public class Consultation {

    @Id
    private String numero;

    private LocalDateTime date;

    private String patientId;

    private String medecinId;

    private List<Prescription> prescriptions = new ArrayList<>();

    private String documentId;

    public Consultation() {
    }

    public Consultation(
            String numero,
            LocalDateTime date,
            String patientId,
            String medecinId) {
        this.numero = numero;
        this.date = date;
        this.patientId = patientId;
        this.medecinId = medecinId;
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

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
