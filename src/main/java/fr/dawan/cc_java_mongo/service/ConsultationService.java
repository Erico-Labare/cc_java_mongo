package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Prescription;
import fr.dawan.cc_java_mongo.repository.ConsultationRepository;
import fr.dawan.cc_java_mongo.repository.MedecinRepository;
import fr.dawan.cc_java_mongo.repository.MedicamentRepository;
import fr.dawan.cc_java_mongo.repository.PatientRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.time.Clock;

/**
 * Service métier pour la gestion des consultations.
 * <p>
 * Ce service encapsule la logique métier liée aux consultations : création,
 * modification, suppression, recherche et validation des données.
 * Il applique des règles métier spécifiques comme l'interdiction de modifier
 * les consultations passées et la validation des références aux patients,
 * médecins et médicaments.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Consultation
 * @see fr.dawan.cc_java_mongo.repository.ConsultationRepository
 */
@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final MedicamentRepository medicamentRepository;
    private final Clock clock;

    /**
     * Construit le service avec les repositories nécessaires.
     *
     * @param consultationRepository le repository des consultations
     * @param patientRepository le repository des patients
     * @param medecinRepository le repository des médecins
     * @param medicamentRepository le repository des médicaments
     */
    public ConsultationService(
            ConsultationRepository consultationRepository,
            PatientRepository patientRepository,
            MedecinRepository medecinRepository,
            MedicamentRepository medicamentRepository) {
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.medicamentRepository = medicamentRepository;
        this.clock = Clock.systemDefaultZone();
    }

    /**
     * Récupère les consultations avec pagination.
     *
     * @param pageable paramètres de pagination et de tri
     * @return page de consultations
     */
    public Page<Consultation> getAllConsultations(Pageable pageable) {
        return consultationRepository.findAll(pageable);
    }

    /**
     * Récupère les consultations d'un patient avec pagination.
     *
     * @param patientId identifiant du patient
     * @param pageable paramètres de pagination et de tri
     * @return page de consultations du patient
     */
    public Page<Consultation> getConsultationsByPatient(
            String patientId, Pageable pageable) {

        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException(
                    "Patient introuvable : " + patientId);
        }

        return consultationRepository.findByPatientId(patientId, pageable);
    }

    /**
     * Recherche une consultation par son numéro.
     *
     * @param numero le numéro de la consultation
     * @return la consultation trouvée
     */
    public Consultation getConsultationById(String numero) {
        return consultationRepository.findById(numero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consultation introuvable : " + numero));
    }

    /**
     * Crée une consultation programmée dans le futur.
     *
     * @param consultation la consultation à créer
     * @return la consultation créée
     */
    public Consultation createConsultation(Consultation consultation) {
        if (consultation.getNumero() == null
                || consultation.getNumero().isBlank()) {
            throw new IllegalArgumentException(
                    "Le numéro de consultation est obligatoire.");
        }

        if (consultationRepository.existsById(consultation.getNumero())) {
            throw new IllegalArgumentException(
                    "Une consultation existe déjà avec ce numéro.");
        }

        validateConsultation(consultation);
        return consultationRepository.save(consultation);
    }

    /**
     * Modifie une consultation programmée dans le futur.
     *
     * @param numero le numéro de la consultation à modifier
     * @param consultation les nouvelles données de la consultation
     * @return la consultation modifiée
     */
    public Consultation updateConsultation(
            String numero, Consultation consultation) {

        Consultation existing = getConsultationById(numero);

        ensureFutureConsultation(existing);
        validateConsultation(consultation);

        existing.setDate(consultation.getDate());
        existing.setPatientId(consultation.getPatientId());
        existing.setMedecinId(consultation.getMedecinId());
        existing.setPrescriptions(
                consultation.getPrescriptions() == null
                        ? new ArrayList<>()
                        : consultation.getPrescriptions());

        // On conserve le numéro et le document éventuellement joint.
        return consultationRepository.save(existing);
    }

    /**
     * Supprime une consultation programmée dans le futur.
     *
     * @param numero le numéro de la consultation à supprimer
     */
    public void deleteConsultation(String numero) {
        Consultation consultation = getConsultationById(numero);
        ensureFutureConsultation(consultation);
        consultationRepository.delete(consultation);
    }

    /**
     * Modifie les prescriptions d'une consultation future.
     *
     * @param numero le numéro de la consultation
     * @param prescriptions la nouvelle liste de prescriptions
     * @return la consultation modifiée
     */
    public Consultation updatePrescriptions(
            String numero, List<Prescription> prescriptions) {

        Consultation consultation = getConsultationById(numero);
        ensureFutureConsultation(consultation);

        validatePrescriptions(prescriptions);
        consultation.setPrescriptions(
                prescriptions == null
                        ? new ArrayList<>()
                        : prescriptions);

        return consultationRepository.save(consultation);
    }


    /**
     * Vérifie les données et les références d'une consultation.
     *
     * @param consultation la consultation à valider
     */
    private void validateConsultation(Consultation consultation) {
        if (consultation.getDate() == null
                || !consultation.getDate().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException(
                    "La date de consultation doit être dans le futur.");
        }

        if (consultation.getPatientId() == null
                || consultation.getPatientId().isBlank()
                || !patientRepository.existsById(consultation.getPatientId())) {
            throw new ResourceNotFoundException(
                    "Patient associé à la consultation introuvable.");
        }

        if (consultation.getMedecinId() == null
                || consultation.getMedecinId().isBlank()
                || !medecinRepository.existsById(consultation.getMedecinId())) {
            throw new ResourceNotFoundException(
                    "Médecin associé à la consultation introuvable.");
        }

        validatePrescriptions(consultation.getPrescriptions());
    }


    /**
     * Vérifie que les médicaments existent et que le nombre de prises est valide.
     *
     * @param prescriptions la liste des prescriptions à valider
     */
    private void validatePrescriptions(List<Prescription> prescriptions) {
        if (prescriptions == null) {
            return;
        }

        for (Prescription prescription : prescriptions) {
            if (prescription.getMedicamentId() == null
                    || prescription.getMedicamentId().isBlank()
                    || !medicamentRepository.existsById(
                    prescription.getMedicamentId())) {
                throw new ResourceNotFoundException(
                        "Médicament prescrit introuvable.");
            }

            if (prescription.getNbPrises() <= 0) {
                throw new IllegalArgumentException(
                        "Le nombre de prises doit être supérieur à zéro.");
            }
        }
    }

    /**
     * Vérifie que la consultation est encore programmée dans le futur.
     *
     * @param consultation la consultation à vérifier
     */
    private void ensureFutureConsultation(Consultation consultation) {
        if (consultation.getDate() == null
                || !consultation.getDate().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException(
                    "Seules les consultations futures peuvent être modifiées ou supprimées.");
        }
    }

}
