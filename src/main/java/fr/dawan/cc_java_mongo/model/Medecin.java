
package fr.dawan.cc_java_mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente un médecin enregistré dans la base MongoDB.
 * <p>
 * Cette classe est mappée à la collection {@code medecins} dans MongoDB.
 * Le matricule sert d'identifiant unique.
 * </p>
 *
 * @see org.springframework.data.mongodb.core.mapping.Document
 */
@Document(collection = "medecins")
public class Medecin {

    @Id
    private String matricule;

    private String nom;

    /**
     * Constructeur par défaut requis par Spring Data MongoDB.
     */
    public Medecin() {
    }

    /**
     * Construit un médecin avec son matricule et son nom.
     *
     * @param matricule le matricule du médecin (identifiant unique)
     * @param nom le nom du médecin
     */
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

