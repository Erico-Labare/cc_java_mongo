
package fr.dawan.cc_java_mongo.dto;

/**
 * DTO pour les données de réponse d'un patient.
 * <p>
 * Cette classe contient les données renvoyées par l'API après une opération
 * sur un patient (création, lecture, modification).
 * </p>
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
