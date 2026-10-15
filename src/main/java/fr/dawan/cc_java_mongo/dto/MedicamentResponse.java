
package fr.dawan.cc_java_mongo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données de réponse d'un médicament.
 * <p>
 * Cette classe contient les données renvoyées par l'API après une opération
 * sur un médicament (création, lecture, modification).
 * </p>
 */
@Schema(name = "MedicamentResponse", description = "Médicament exposé par le catalogue.")
public class MedicamentResponse {

    @Schema(description = "Code du médicament.", example = "MED-001")
    private String code;
    @Schema(description = "Libellé du médicament.", example = "Paracétamol 500 mg")
    private String libelle;

    public MedicamentResponse() {
    }

    public MedicamentResponse(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
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

