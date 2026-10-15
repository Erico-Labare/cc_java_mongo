
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données de création ou modification d'un patient.
 * <p>
 * Cette classe contient les données reçues du client pour créer ou modifier
 * un patient. Les champs sont validés par les annotations Jakarta Validation.
 * </p>
 */
@Schema(name = "PatientRequest", description = "Données nécessaires à la création ou à la mise à jour d'un dossier patient.")
public class PatientRequest {

    @NotBlank(message = "Le numéro de Sécurité sociale est obligatoire.")
    @Pattern(
            regexp = "^\\d{15}$",
            message = "Le numéro de Sécurité sociale doit contenir 15 chiffres."
    )
    @Schema(description = "Numéro de sécurité sociale utilisé comme identifiant ; exactement 15 chiffres.", example = "123456789012345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numeroSecuriteSociale;

    @NotBlank(message = "Le nom du patient est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    @Schema(description = "Nom du patient, limité à 100 caractères.", example = "Martin", requiredMode = Schema.RequiredMode.REQUIRED)
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
