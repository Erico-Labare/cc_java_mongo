
package fr.dawan.cc_java_mongo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données de réponse d'un médecin.
 * <p>
 * Cette classe contient les données renvoyées par l'API après une opération
 * sur un médecin (création, lecture, modification).
 * </p>
 */
@Schema(name = "MedecinResponse", description = "Informations du médecin exposées par l'API.")
public class MedecinResponse {

    @Schema(description = "Matricule unique du médecin.", example = "MED-1042")
    private String matricule;
    @Schema(description = "Nom du médecin.", example = "Dr Dupont")
    private String nom;

    public MedecinResponse() {
    }

    public MedecinResponse(String matricule, String nom) {
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

