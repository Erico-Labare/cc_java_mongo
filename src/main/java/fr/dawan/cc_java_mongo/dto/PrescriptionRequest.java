
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO pour les données d'une prescription de médicament.
 * <p>
 * Cette classe contient les données d'une prescription incluse dans une
 * consultation. Le nombre de prises doit être positif.
 * </p>
 */
public class PrescriptionRequest {

    @NotBlank(message = "L'identifiant du médicament est obligatoire.")
    private String medicamentId;

    @Positive(message = "Le nombre de prises doit être supérieur à zéro.")
    private int nbPrises;

    public PrescriptionRequest() {
        // Default constructor required for JSON deserialization
    }

    public String getMedicamentId() {
        return medicamentId;
    }

    public void setMedicamentId(String medicamentId) {
        this.medicamentId = medicamentId;
    }

    public int getNbPrises() {
        return nbPrises;
    }

    public void setNbPrises(int nbPrises) {
        this.nbPrises = nbPrises;
    }
}

