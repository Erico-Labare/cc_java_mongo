
package fr.dawan.cc_java_mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente un médicament disponible dans le catalogue.
 * <p>
 * Cette classe est mappée à la collection {@code medicaments} dans MongoDB.
 * Le code sert d'identifiant unique.
 * </p>
 *
 * @see org.springframework.data.mongodb.core.mapping.Document
 */
@Document(collection = "medicaments")
public class Medicament {

    @Id
    private String code;

    private String libelle;

    /**
     * Constructeur par défaut requis par Spring Data MongoDB.
     */
    public Medicament() {
    }

    /**
     * Construit un médicament avec son code et son libellé.
     *
     * @param code le code du médicament (identifiant unique)
     * @param libelle le libellé ou nom du médicament
     */
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

