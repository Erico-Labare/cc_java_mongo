
package fr.dawan.cc_java_mongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les données de création ou modification d'un médecin.
 * <p>
 * Cette classe contient les données reçues du client pour créer ou modifier
 * un médecin. Les champs sont validés par les annotations Jakarta Validation.
 * </p>
 */
@Schema(name = "MedecinRequest", description = "Données nécessaires à la création ou à la modification d'un médecin.")
public class MedecinRequest {

    @NotBlank(message = "Le matricule est obligatoire.")
    @Size(max = 50, message = "Le matricule ne doit pas dépasser 50 caractères.")
    @Schema(description = "Matricule unique, limité à 50 caractères.", example = "MED-1042", requiredMode = Schema.RequiredMode.REQUIRED)
    private String matricule;

    @NotBlank(message = "Le nom du médecin est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    @Schema(description = "Nom du médecin, limité à 100 caractères.", example = "Dr Dupont", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nom;

    public MedecinRequest() {
        // Default constructor required for JSON deserialization
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

