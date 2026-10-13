
package fr.dawan.cc_java_mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente un médicament disponible dans le catalogue.
 */
@Document(collection = "medicaments")
public class Medicament {

    @Id
    private String code;

    private String libelle;

    public Medicament() {
    }

    public Medicament(String code, String libelle) {
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

