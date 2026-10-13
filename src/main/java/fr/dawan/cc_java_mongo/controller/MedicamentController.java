
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.MedicamentRequest;
import fr.dawan.cc_java_mongo.dto.MedicamentResponse;
import fr.dawan.cc_java_mongo.model.Medicament;
import fr.dawan.cc_java_mongo.service.MedicamentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expose les endpoints REST de gestion des médicaments.
 */
@RestController
@RequestMapping("/api/medicaments")
public class MedicamentController {

    private final MedicamentService medicamentService;

    public MedicamentController(MedicamentService medicamentService) {
        this.medicamentService = medicamentService;
    }

    /**
     * Récupère la liste des médicaments.
     */
    @GetMapping
    public List<MedicamentResponse> getMedicaments() {
        return medicamentService.getAllMedicaments()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Récupère un médicament par son code.
     */
    @GetMapping("/{code}")
    public MedicamentResponse getMedicament(@PathVariable String code) {
        return toResponse(medicamentService.getMedicamentById(code));
    }

    /**
     * Crée un médicament.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicamentResponse createMedicament(
            @Valid @RequestBody MedicamentRequest request) {

        Medicament medicament = new Medicament(
                request.getCode(),
                request.getLibelle());

        return toResponse(medicamentService.createMedicament(medicament));
    }

    /**
     * Modifie un médicament existant.
     */
    @PutMapping("/{code}")
    public MedicamentResponse updateMedicament(
            @PathVariable String code,
            @Valid @RequestBody MedicamentRequest request) {

        Medicament medicament = new Medicament(
                code,
                request.getLibelle());

        return toResponse(
                medicamentService.updateMedicament(code, medicament));
    }

    /**
     * Supprime un médicament.
     */
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicament(@PathVariable String code) {
        medicamentService.deleteMedicament(code);
    }

    /**
     * Convertit un modèle Medicament en DTO de réponse.
     */
    private MedicamentResponse toResponse(Medicament medicament) {
        return new MedicamentResponse(
                medicament.getCode(),
                medicament.getLibelle());
    }
}

