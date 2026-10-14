package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Medecin;
import fr.dawan.cc_java_mongo.repository.MedecinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service métier pour la gestion des médecins.
 * <p>
 * Ce service encapsule la logique métier liée aux médecins : création,
 * modification, suppression, recherche et validation des données.
 * Il utilise le repository pour les opérations de persistance.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Medecin
 * @see fr.dawan.cc_java_mongo.repository.MedecinRepository
 */
@Service
public class MedecinService {

    private final MedecinRepository medecinRepository;

    /**
     * Construit le service avec le repository des médecins.
     *
     * @param medecinRepository le repository pour accéder aux médecins
     */
    public MedecinService(MedecinRepository medecinRepository) {
        this.medecinRepository = medecinRepository;
    }

    /**
     * Recherche tous les médecins avec pagination.
     *
     * @param pageable paramètres de pagination
     * @return page de médecins
     */
    public Page<Medecin> getAllMedecins(Pageable pageable) {
        return medecinRepository.findAll(pageable);
    }

    /**
     * Recherche les médecins par nom, sans tenir compte de la casse.
     *
     * @param nom nom ou partie du nom recherché
     * @param pageable paramètres de pagination
     * @return page de médecins correspondants
     */
    public Page<Medecin> searchMedecins(String nom, Pageable pageable) {
        return medecinRepository.findByNomContainingIgnoreCase(
                nom == null ? "" : nom.trim(),
                pageable
        );
    }

    /**
     * Recherche un médecin par son matricule.
     *
     * @param matricule identifiant du médecin
     * @return médecin trouvé
     * @throws ResourceNotFoundException si le médecin n'existe pas
     */
    public Medecin getMedecinById(String matricule) {
        return medecinRepository.findById(matricule)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Médecin introuvable : " + matricule
                ));
    }

    /**
     * Enregistre un nouveau médecin.
     *
     * @param medecin médecin à enregistrer
     * @return médecin enregistré
     */
    public Medecin createMedecin(Medecin medecin) {
        if (medecin.getMatricule() == null
                || medecin.getMatricule().isBlank()) {
            throw new IllegalArgumentException(
                    "Le matricule du médecin est obligatoire."
            );
        }

        if (medecinRepository.existsById(medecin.getMatricule())) {
            throw new IllegalArgumentException(
                    "Un médecin existe déjà avec ce matricule."
            );
        }

        return medecinRepository.save(medecin);
    }

    /**
     * Met à jour le nom d'un médecin existant.
     *
     * @param matricule identifiant du médecin
     * @param medecin nouvelles données
     * @return médecin mis à jour
     */
    public Medecin updateMedecin(String matricule, Medecin medecin) {
        Medecin existingMedecin = getMedecinById(matricule);
        existingMedecin.setNom(medecin.getNom());

        return medecinRepository.save(existingMedecin);
    }

    /**
     * Supprime un médecin existant.
     *
     * @param matricule identifiant du médecin
     */
    public void deleteMedecin(String matricule) {
        Medecin medecin = getMedecinById(matricule);
        medecinRepository.delete(medecin);
    }
}
