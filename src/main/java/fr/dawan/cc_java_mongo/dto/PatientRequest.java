
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO pour les données de création ou modification d'un patient.
 * <p>
 * Cette classe contient les données reçues du client pour créer ou modifier
 * un patient. Les champs sont validés par les annotations Jakarta Validation.
 * </p>
 */
public class PatientRequest {

    @NotBlank(message = "Le numéro de Sécurité sociale est obligatoire.")
    @Pattern(
            regexp = "^\\d{15}$",
            message = "Le numéro de Sécurité sociale doit contenir 15 chiffres."
    )
    private String numeroSecuriteSociale;

    @NotBlank(message = "Le nom du patient est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    private String nom;

    public PatientRequest() {
        // Default constructor required for JSON deserialization
    }

    public String getNumeroSecuriteSociale() {
        return numeroSecuriteSociale;
    }

    public void setNumeroSecuriteSociale(String numeroSecuriteSociale) {
        this.numeroSecuriteSociale = numeroSecuriteSociale;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
