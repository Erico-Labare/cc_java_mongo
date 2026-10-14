package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Medicament;
import fr.dawan.cc_java_mongo.repository.MedicamentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service métier pour la gestion des médicaments.
 * <p>
 * Ce service encapsule la logique métier liée aux médicaments : création,
 * modification, suppression, recherche et validation des données.
 * Il utilise le repository pour les opérations de persistance.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Medicament
 * @see fr.dawan.cc_java_mongo.repository.MedicamentRepository
 */
@Service
public class MedicamentService {

    private final MedicamentRepository medicamentRepository;

    /**
     * Construit le service avec le repository des médicaments.
     *
     * @param medicamentRepository le repository pour accéder aux médicaments
     */
    public MedicamentService(MedicamentRepository medicamentRepository) {
        this.medicamentRepository = medicamentRepository;
    }

    /**
     * Récupère tous les médicaments.
     *
     * @return liste des médicaments
     */
    public List<Medicament> getAllMedicaments() {
        return medicamentRepository.findAll();
    }

    /**
     * Recherche un médicament par son code.
     *
     * @param code identifiant du médicament
     * @return médicament trouvé
     */
    public Medicament getMedicamentById(String code) {
        return medicamentRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Médicament introuvable : " + code
                ));
    }

    /**
     * Enregistre un nouveau médicament.
     *
     * @param medicament médicament à enregistrer
     * @return médicament enregistré
     */
    public Medicament createMedicament(Medicament medicament) {
        if (medicament.getCode() == null
                || medicament.getCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Le code du médicament est obligatoire."
            );
        }

        if (medicamentRepository.existsById(medicament.getCode())) {
            throw new IllegalArgumentException(
                    "Un médicament existe déjà avec ce code."
            );
        }

        return medicamentRepository.save(medicament);
    }

    /**
     * Met à jour le libellé d'un médicament existant.
     *
     * @param code identifiant du médicament
     * @param medicament nouvelles données
     * @return médicament mis à jour
     */
    public Medicament updateMedicament(String code, Medicament medicament) {
        Medicament existingMedicament = getMedicamentById(code);

        existingMedicament.setLibelle(medicament.getLibelle());

        return medicamentRepository.save(existingMedicament);
    }

    /**
     * Supprime un médicament existant.
     *
     * @param code identifiant du médicament
     */
    public void deleteMedicament(String code) {
        Medicament medicament = getMedicamentById(code);
        medicamentRepository.delete(medicament);
    }
}
