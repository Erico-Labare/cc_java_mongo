package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.exception.AccessDeniedException;
import fr.dawan.cc_java_mongo.model.Patient;
import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.repository.PatientRepository;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import fr.dawan.cc_java_mongo.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service métier pour la gestion des patients.
 * <p>
 * Ce service encapsule la logique métier liée aux patients : création,
 * modification, suppression, recherche et validation des données.
 * Il utilise le repository pour les opérations de persistance.
 * </p>
 * <p>
 * <strong>Contrôles d'accès :</strong>
 * <ul>
 * <li>Un patient ne peut voir que son propre dossier</li>
 * <li>Un médecin peut voir tous les patients (pour son activité)</li>
 * <li>Un administrateur peut voir tous les patients</li>
 * </ul>
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Patient
 * @see fr.dawan.cc_java_mongo.repository.PatientRepository
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Construit le service avec le repository des patients.
     *
     * @param patientRepository le repository pour accéder aux patients
     * @param utilisateurRepository le repository pour accéder aux utilisateurs
     */
    public PatientService(
            PatientRepository patientRepository,
            UtilisateurRepository utilisateurRepository) {
        this.patientRepository = patientRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Recherche les patients avec pagination.
     * <p>
     * Les résultats sont filtrés selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : retourne uniquement son propre dossier</li>
     * <li>MEDECIN : retourne tous les patients</li>
     * <li>ADMIN : retourne tous les patients</li>
     * </ul>
     * </p>
     *
     * @param pageable paramètres de pagination
     * @return page de patients filtrée selon les droits de l'utilisateur
     */
    public Page<Patient> getAllPatients(Pageable pageable) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            String patientId = SecurityUtils.getCurrentPatientId(utilisateurRepository);
            if (patientId != null) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null) {
                    return new org.springframework.data.domain.PageImpl<>(java.util.List.of(patient));
                }
            }
            return Page.empty(pageable);
        }

        if (role == Role.MEDECIN) {
            String medecinId = SecurityUtils.getCurrentMedecinId(utilisateurRepository);
            if (medecinId == null) {
                return Page.empty(pageable);
            }
            java.util.List<Patient> patients = patientRepository.findByMedecinId(medecinId);
            return new org.springframework.data.domain.PageImpl<>(patients, pageable, patients.size());
        }

        return patientRepository.findAll(pageable);
    }

    /**
     * Recherche les patients par nom avec pagination.
     * <p>
     * Les résultats sont filtrés selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : recherche uniquement dans son propre dossier</li>
     * <li>MEDECIN : recherche dans tous les patients</li>
     * <li>ADMIN : recherche dans tous les patients</li>
     * </ul>
     * </p>
     *
     * @param nom nom ou partie du nom recherché
     * @param pageable paramètres de pagination
     * @return page de patients correspondants filtrée selon les droits
     */
    public Page<Patient> searchPatients(String nom, Pageable pageable) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            String patientId = SecurityUtils.getCurrentPatientId(utilisateurRepository);
            if (patientId != null) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && (nom == null || nom.isBlank()
                        || patient.getNom().toLowerCase().contains(nom.toLowerCase()))) {
                    return new org.springframework.data.domain.PageImpl<>(java.util.List.of(patient));
                }
            }
            return Page.empty(pageable);
        }

        if (role == Role.MEDECIN) {
            String medecinId = SecurityUtils.getCurrentMedecinId(utilisateurRepository);
            if (medecinId == null) {
                return Page.empty(pageable);
            }
            java.util.List<Patient> patients = patientRepository.findByMedecinId(medecinId)
                    .stream()
                    .filter(patient -> patient.getNom() != null
                            && patient.getNom().toLowerCase().contains(nom == null ? "" : nom.toLowerCase()))
                    .toList();
            return new org.springframework.data.domain.PageImpl<>(patients, pageable, patients.size());
        }

        return patientRepository.findByNomContainingIgnoreCase(
                nom == null ? "" : nom.trim(),
                pageable
        );
    }

    /**
     * Recherche un patient par son numéro de Sécurité sociale.
     * <p>
     * L'accès est contrôlé selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : ne peut accéder qu'à son propre dossier</li>
     * <li>MEDECIN : peut accéder à tous les patients</li>
     * <li>ADMIN : peut accéder à tous les patients</li>
     * </ul>
     * </p>
     *
     * @param numeroSecuriteSociale identifiant du patient
     * @return patient trouvé
     * @throws ResourceNotFoundException si le patient n'existe pas
     * @throws IllegalStateException si l'utilisateur n'a pas les droits d'accès
     */
    public Patient getPatientById(String numeroSecuriteSociale) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            String patientId = SecurityUtils.getCurrentPatientId(utilisateurRepository);
            if (patientId == null || !patientId.equals(numeroSecuriteSociale)) {
                throw new AccessDeniedException(
                        "Un patient ne peut accéder qu'à son propre dossier.");
            }
        }

        if (role == Role.MEDECIN) {
            String medecinId = SecurityUtils.getCurrentMedecinId(utilisateurRepository);
            Patient linkedPatient = patientRepository.findById(numeroSecuriteSociale)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Patient introuvable : " + numeroSecuriteSociale));
            if (medecinId == null || !medecinId.equals(linkedPatient.getMedecinId())) {
                throw new AccessDeniedException("Accès à ce dossier patient non autorisé.");
            }
            return linkedPatient;
        }

        return patientRepository.findById(numeroSecuriteSociale)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient introuvable : " + numeroSecuriteSociale
                ));
    }

    /**
     * Enregistre un nouveau patient.
     *
     * @param patient patient à enregistrer
     * @return patient enregistré
     */
    public Patient createPatient(Patient patient) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);
        if (role == Role.PATIENT || role == Role.MEDECIN) {
            throw new AccessDeniedException("Seul un administrateur peut créer un dossier patient.");
        }

        if (patient.getNumeroSecuriteSociale() == null
                || patient.getNumeroSecuriteSociale().isBlank()) {
            throw new IllegalArgumentException(
                    "Le numéro de Sécurité sociale est obligatoire."
            );
        }

        if (patientRepository.existsById(
                patient.getNumeroSecuriteSociale())) {
            throw new IllegalArgumentException(
                    "Un patient existe déjà avec ce numéro de Sécurité sociale."
            );
        }

        return patientRepository.save(patient);
    }

    /**
     * Met à jour le nom d'un patient existant.
     *
     * @param numeroSecuriteSociale identifiant du patient
     * @param patient données à mettre à jour
     * @return patient mis à jour
     */
    public Patient updatePatient(
            String numeroSecuriteSociale,
            Patient patient) {

        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);
        if (role != Role.ADMIN && role != Role.PATIENT) {
            throw new AccessDeniedException("Modification du dossier patient non autorisée.");
        }

        Patient existingPatient = getPatientById(numeroSecuriteSociale);

        existingPatient.setNom(patient.getNom());

        return patientRepository.save(existingPatient);
    }

    /**
     * Supprime un patient existant.
     *
     * @param numeroSecuriteSociale identifiant du patient
     */
    public void deletePatient(String numeroSecuriteSociale) {
        if (SecurityUtils.getCurrentRole(utilisateurRepository) != Role.ADMIN) {
            throw new AccessDeniedException("Seul un administrateur peut supprimer un dossier patient.");
        }
        Patient patient = getPatientById(numeroSecuriteSociale);
        patientRepository.delete(patient);
    }
}

