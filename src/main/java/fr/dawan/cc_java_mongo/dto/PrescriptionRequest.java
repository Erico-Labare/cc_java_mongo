
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données d'une prescription de médicament.
 * <p>
 * Cette classe contient les données d'une prescription incluse dans une
 * consultation. Le nombre de prises doit être positif.
 * </p>
 */
@Schema(name = "PrescriptionRequest", description = "Référence d'un médicament prescrit et nombre de prises.")
public class PrescriptionRequest {

    @NotBlank(message = "L'identifiant du médicament est obligatoire.")
    @Schema(description = "Code d'un médicament existant dans le catalogue.", example = "MED-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String medicamentId;

    @Positive(message = "Le nombre de prises doit être supérieur à zéro.")
    @Schema(description = "Nombre de prises, strictement positif.", example = "3", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
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

