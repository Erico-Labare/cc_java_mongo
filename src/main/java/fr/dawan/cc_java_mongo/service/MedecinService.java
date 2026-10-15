package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.exception.AccessDeniedException;
import fr.dawan.cc_java_mongo.model.Medecin;
import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.repository.MedecinRepository;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import fr.dawan.cc_java_mongo.security.SecurityUtils;
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
 * <p>
 * <strong>Contrôles d'accès :</strong>
 * <ul>
 * <li>PATIENT : ne peut pas accéder aux données des médecins</li>
 * <li>MEDECIN : peut voir tous les médecins</li>
 * <li>ADMIN : peut voir tous les médecins</li>
 * </ul>
 *
 * @see fr.dawan.cc_java_mongo.model.Medecin
 * @see fr.dawan.cc_java_mongo.repository.MedecinRepository
 */
@Service
public class MedecinService {

    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Construit le service avec le repository des médecins.
     *
     * @param medecinRepository le repository pour accéder aux médecins
     * @param utilisateurRepository le repository pour accéder aux utilisateurs
     */
    public MedecinService(
            MedecinRepository medecinRepository,
            UtilisateurRepository utilisateurRepository) {
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Recherche tous les médecins avec pagination.
     * <p>
     * Les résultats sont filtrés selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : accès refusé</li>
     * <li>MEDECIN : retourne tous les médecins</li>
     * <li>ADMIN : retourne tous les médecins</li>
     * </ul>
     *
     * @param pageable paramètres de pagination
     * @return page de médecins filtrée selon les droits
     * @throws IllegalStateException si un patient tente d'accéder aux données
     */
    public Page<Medecin> getAllMedecins(Pageable pageable) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            throw new AccessDeniedException(
                    "Un patient ne peut pas accéder aux données des médecins.");
        }

        return medecinRepository.findAll(pageable);
    }

    /**
     * Recherche les médecins par nom, sans tenir compte de la casse.
     * <p>
     * Les résultats sont filtrés selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : accès refusé</li>
     * <li>MEDECIN : recherche dans tous les médecins</li>
     * <li>ADMIN : recherche dans tous les médecins</li>
     * </ul>
     *
     * @param nom nom ou partie du nom recherché
     * @param pageable paramètres de pagination
     * @return page de médecins correspondants filtrée selon les droits
     * @throws IllegalStateException si un patient tente d'accéder aux données
     */
    public Page<Medecin> searchMedecins(String nom, Pageable pageable) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            throw new AccessDeniedException(
                    "Un patient ne peut pas accéder aux données des médecins.");
        }

        return medecinRepository.findByNomContainingIgnoreCase(
                nom == null ? "" : nom.trim(),
                pageable
        );
    }

    /**
     * Recherche un médecin par son matricule.
     * <p>
     * L'accès est contrôlé selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : accès refusé</li>
     * <li>MEDECIN : peut accéder à tous les médecins</li>
     * <li>ADMIN : peut accéder à tous les médecins</li>
     * </ul>
     *
     * @param matricule identifiant du médecin
     * @return médecin trouvé
     * @throws ResourceNotFoundException si le médecin n'existe pas
     * @throws IllegalStateException si un patient tente d'accéder aux données
     */
    public Medecin getMedecinById(String matricule) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            throw new AccessDeniedException(
                    "Un patient ne peut pas accéder aux données des médecins.");
        }

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
        requireAdmin();
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
        requireAdmin();
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
        requireAdmin();
        Medecin medecin = getMedecinById(matricule);
        medecinRepository.delete(medecin);
    }

    private void requireAdmin() {
        if (SecurityUtils.getCurrentRole(utilisateurRepository) != Role.ADMIN) {
            throw new AccessDeniedException("Seul un administrateur peut gérer les profils médecins.");
        }
    }
}
