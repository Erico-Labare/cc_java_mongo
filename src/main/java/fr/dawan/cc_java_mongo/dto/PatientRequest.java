
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Données reçues pour créer ou modifier un patient.
 */
public class PatientRequest {

    @NotBlank(message = "Le numéro de Sécurité sociale est obligatoire.")
    @Pattern(
            regexp = "^[0-9]{15}$",
            message = "Le numéro de Sécurité sociale doit contenir 15 chiffres."
    )
    private String numeroSecuriteSociale;

    @NotBlank(message = "Le nom du patient est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    private String nom;

    public PatientRequest() {
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
