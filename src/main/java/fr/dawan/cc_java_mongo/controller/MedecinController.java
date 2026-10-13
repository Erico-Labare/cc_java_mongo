
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.MedecinRequest;
import fr.dawan.cc_java_mongo.dto.MedecinResponse;
import fr.dawan.cc_java_mongo.model.Medecin;
import fr.dawan.cc_java_mongo.service.MedecinService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Expose les endpoints REST de gestion des médecins.
 */
@RestController
@RequestMapping("/api/medecins")
public class MedecinController {

    private final MedecinService medecinService;

    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }

    /**
     * Récupère les médecins avec pagination et recherche facultative.
     */
    @GetMapping
    public Page<MedecinResponse> getMedecins(
            @RequestParam(required = false) String nom,
            @PageableDefault(size = 10, sort = "nom") Pageable pageable) {

        Page<Medecin> medecins = (nom == null || nom.isBlank())
                ? medecinService.getAllMedecins(pageable)
                : medecinService.searchMedecins(nom, pageable);

        return medecins.map(this::toResponse);
    }

    /**
     * Récupère un médecin par son matricule.
     */
    @GetMapping("/{matricule}")
    public MedecinResponse getMedecin(@PathVariable String matricule) {
        return toResponse(medecinService.getMedecinById(matricule));
    }

    /**
     * Crée un médecin.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedecinResponse createMedecin(
            @Valid @RequestBody MedecinRequest request) {

        Medecin medecin = new Medecin(
                request.getMatricule(),
                request.getNom());

        return toResponse(medecinService.createMedecin(medecin));
    }

    /**
     * Modifie un médecin existant.
     */
    @PutMapping("/{matricule}")
    public MedecinResponse updateMedecin(
            @PathVariable String matricule,
            @Valid @RequestBody MedecinRequest request) {

        Medecin medecin = new Medecin(matricule, request.getNom());

        return toResponse(
                medecinService.updateMedecin(matricule, medecin));
    }

    /**
     * Supprime un médecin.
     */
    @DeleteMapping("/{matricule}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedecin(@PathVariable String matricule) {
        medecinService.deleteMedecin(matricule);
    }

    /**
     * Convertit un modèle Medecin en DTO de réponse.
     */
    private MedecinResponse toResponse(Medecin medecin) {
        return new MedecinResponse(
                medecin.getMatricule(),
                medecin.getNom());
    }
}
