
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données de création ou modification d'un médicament.
 * <p>
 * Cette classe contient les données reçues du client pour créer ou modifier
 * un médicament. Les champs sont validés par les annotations Jakarta Validation.
 * </p>
 */
@Schema(name = "MedicamentRequest", description = "Données nécessaires à la création ou à la modification d'un médicament.")
public class MedicamentRequest {

    @NotBlank(message = "Le code du médicament est obligatoire.")
    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères.")
    @Schema(description = "Code unique, limité à 50 caractères.", example = "MED-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "Le libellé est obligatoire.")
    @Size(max = 150, message = "Le libellé ne doit pas dépasser 150 caractères.")
    @Schema(description = "Libellé du médicament, limité à 150 caractères.", example = "Paracétamol 500 mg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String libelle;

    public MedicamentRequest() {
        // Default constructor required for JSON deserialization
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
