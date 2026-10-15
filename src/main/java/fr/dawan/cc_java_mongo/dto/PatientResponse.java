
package fr.dawan.cc_java_mongo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données de réponse d'un patient.
 * <p>
 * Cette classe contient les données renvoyées par l'API après une opération
 * sur un patient (création, lecture, modification).
 * </p>
 */
@Schema(name = "PatientResponse", description = "Informations du patient exposées par l'API.")
public class PatientResponse {

    @Schema(description = "Numéro de sécurité sociale du patient.", example = "123456789012345")
    private String numeroSecuriteSociale;
    @Schema(description = "Nom du patient.", example = "Martin")
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
