
package fr.dawan.cc_java_mongo.dto;

/**
 * DTO pour les données de réponse d'un médecin.
 * <p>
 * Cette classe contient les données renvoyées par l'API après une opération
 * sur un médecin (création, lecture, modification).
 * </p>
 */
public class MedecinResponse {

    private String matricule;
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

