
package fr.dawan.cc_java_mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Représente un patient enregistré dans la base MongoDB.
 * <p>
 * Cette classe est mappée à la collection {@code patients} dans MongoDB.
 * Le numéro de Sécurité sociale sert d'identifiant unique.
 * </p>
 *
 * @see org.springframework.data.mongodb.core.mapping.Document
 */
@Document(collection = "patients")
public class Patient {

    @Id
    private String numeroSecuriteSociale;

    private String nom;
    private String medecinId;

    /**
     * Constructeur par défaut requis par Spring Data MongoDB.
     */
    public Patient() {
    }

    /**
     * Construit un patient avec son identifiant et son nom.
     *
     * @param numeroSecuriteSociale le numéro de Sécurité sociale (identifiant unique)
     * @param nom le nom du patient
     */
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

    public String getMedecinId() { return medecinId; }

    public void setMedecinId(String medecinId) { this.medecinId = medecinId; }
}

