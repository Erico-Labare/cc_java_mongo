
package fr.dawan.cc_java_mongo.dto;

/**
 * Données renvoyées par l'API pour un patient.
 */
public class PatientResponse {

    private String numeroSecuriteSociale;
    private String nom;

    public PatientResponse() {
    }

    public PatientResponse(String numeroSecuriteSociale, String nom) {
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
