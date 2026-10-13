
package fr.dawan.cc_java_mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente un patient enregistré dans la base MongoDB.
 */
@Document(collection = "patients")
public class Patient {

    @Id
    private String numeroSecuriteSociale;

    private String nom;

    public Patient() {
    }

    public Patient(String numeroSecuriteSociale, String nom) {
        this.numeroSecuriteSociale = numeroSecuriteSociale;
        this.nom = nom;
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

