
package fr.dawan.cc_java_mongo.dto;

/**
 * Données renvoyées par l'API pour un médicament.
 */
public class MedicamentResponse {

    private String code;
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

