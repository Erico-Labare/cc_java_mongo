
package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Patient;
import fr.dawan.cc_java_mongo.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Gère les opérations métier relatives aux patients.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Recherche les patients avec pagination.
     *
     * @param pageable paramètres de pagination
     * @return page de patients
     */
    public Page<Patient> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    /**
     * Recherche les patients par nom avec pagination.
     *
     * @param nom nom ou partie du nom recherché
     * @param pageable paramètres de pagination
     * @return page de patients correspondants
     */
    public Page<Patient> searchPatients(String nom, Pageable pageable) {
        return patientRepository.findByNomContainingIgnoreCase(
                nom == null ? "" : nom.trim(),
                pageable
        );
    }

    /**
     * Recherche un patient par son numéro de Sécurité sociale.
     *
     * @param numeroSecuriteSociale identifiant du patient
     * @return patient trouvé
     * @throws ResourceNotFoundException si le patient n'existe pas
     */
    public Patient getPatientById(String numeroSecuriteSociale) {
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
        Patient patient = getPatientById(numeroSecuriteSociale);
        patientRepository.delete(patient);
    }
}

