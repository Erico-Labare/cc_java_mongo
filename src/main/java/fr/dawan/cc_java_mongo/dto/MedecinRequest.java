
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données reçues pour créer ou modifier un médecin.
 */
public class MedecinRequest {

    @NotBlank(message = "Le matricule est obligatoire.")
    @Size(max = 50, message = "Le matricule ne doit pas dépasser 50 caractères.")
    private String matricule;

    @NotBlank(message = "Le nom du médecin est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    private String nom;

    public MedecinRequest() {
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

