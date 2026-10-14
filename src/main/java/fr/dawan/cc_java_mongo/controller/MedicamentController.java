
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
 * Contrôleur REST pour la gestion des médicaments.
 * <p>
 * Ce contrôleur expose les endpoints CRUD pour les médicaments :
 * </p>
 * <ul>
 * <li>{@code GET /api/medicaments} - Liste de tous les médicaments</li>
 * <li>{@code GET /api/medicaments/{code}} - Détails d'un médicament</li>
 * <li>{@code POST /api/medicaments} - Création d'un médicament</li>
 * <li>{@code PUT /api/medicaments/{code}} - Modification d'un médicament</li>
 * <li>{@code DELETE /api/medicaments/{code}} - Suppression d'un médicament</li>
 * </ul>
 * <p>
 * Tous les endpoints nécessitent une authentification JWT valide.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.service.MedicamentService
 * @see fr.dawan.cc_java_mongo.dto.MedicamentRequest
 * @see fr.dawan.cc_java_mongo.dto.MedicamentResponse
 */
@RestController
@RequestMapping("/api/medicaments")
public class MedicamentController {

    private final MedicamentService medicamentService;

    /**
     * Construit le contrôleur avec le service des médicaments.
     *
     * @param medicamentService le service métier des médicaments
     */
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

