
package fr.dawan.cc_java_mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente un médecin enregistré dans la base MongoDB.
 */
@Document(collection = "medecins")
public class Medecin {

    @Id
    private String matricule;

    private String nom;

    public Medecin() {
    }

    public Medecin(String matricule, String nom) {
        this.matricule = matricule;
        this.nom = nom;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}

