
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données reçues pour créer ou modifier un médicament.
 */
public class MedicamentRequest {

    @NotBlank(message = "Le code du médicament est obligatoire.")
    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères.")
    private String code;

    @NotBlank(message = "Le libellé est obligatoire.")
    @Size(max = 150, message = "Le libellé ne doit pas dépasser 150 caractères.")
    private String libelle;

    public MedicamentRequest() {
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
