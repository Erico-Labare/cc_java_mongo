package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.exception.AccessDeniedException;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Prescription;
import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.repository.ConsultationRepository;
import fr.dawan.cc_java_mongo.repository.MedecinRepository;
import fr.dawan.cc_java_mongo.repository.MedicamentRepository;
import fr.dawan.cc_java_mongo.repository.PatientRepository;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import fr.dawan.cc_java_mongo.security.SecurityUtils;

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
 * <p>
 * <strong>Contrôles d'accès :</strong>
 * <ul>
 * <li>PATIENT : ne peut voir que ses propres consultations</li>
 * <li>MEDECIN : peut voir toutes les consultations</li>
 * <li>ADMIN : peut voir toutes les consultations</li>
 * </ul>
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
    private final UtilisateurRepository utilisateurRepository;
    private final Clock clock;

    /**
     * Construit le service avec les repositories nécessaires.
     *
     * @param consultationRepository le repository des consultations
     * @param patientRepository le repository des patients
     * @param medecinRepository le repository des médecins
     * @param medicamentRepository le repository des médicaments
     * @param utilisateurRepository le repository des utilisateurs
     */
    public ConsultationService(
            ConsultationRepository consultationRepository,
            PatientRepository patientRepository,
            MedecinRepository medecinRepository,
            MedicamentRepository medicamentRepository,
            UtilisateurRepository utilisateurRepository) {
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.medicamentRepository = medicamentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.clock = Clock.systemDefaultZone();
    }

    /**
     * Récupère les consultations avec pagination.
     * <p>
     * Les résultats sont filtrés selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : retourne uniquement ses propres consultations</li>
     * <li>MEDECIN : retourne toutes les consultations</li>
     * <li>ADMIN : retourne toutes les consultations</li>
     * </ul>
     * </p>
     *
     * @param pageable paramètres de pagination et de tri
     * @return page de consultations filtrée selon les droits
     */
    public Page<Consultation> getAllConsultations(Pageable pageable) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            String patientId = SecurityUtils.getCurrentPatientId(utilisateurRepository);
            if (patientId != null) {
                return consultationRepository.findByPatientId(patientId, pageable);
            }
            return Page.empty(pageable);
        }

        if (role == Role.MEDECIN) {
            String medecinId = SecurityUtils.getCurrentMedecinId(utilisateurRepository);
            return medecinId == null ? Page.empty(pageable)
                    : consultationRepository.findByMedecinId(medecinId, pageable);
        }

        return consultationRepository.findAll(pageable);
    }

    /**
     * Récupère les consultations d'un patient avec pagination.
     * <p>
     * L'accès est contrôlé selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : ne peut accéder qu'à ses propres consultations</li>
     * <li>MEDECIN : peut accéder à toutes les consultations</li>
     * <li>ADMIN : peut accéder à toutes les consultations</li>
     * </ul>
     * </p>
     *
     * @param patientId identifiant du patient
     * @param pageable paramètres de pagination et de tri
     * @return page de consultations du patient
     * @throws ResourceNotFoundException si le patient n'existe pas
     * @throws IllegalStateException si l'utilisateur n'a pas les droits d'accès
     */
    public Page<Consultation> getConsultationsByPatient(
            String patientId, Pageable pageable) {

        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            String currentPatientId = SecurityUtils.getCurrentPatientId(utilisateurRepository);
            if (currentPatientId == null || !currentPatientId.equals(patientId)) {
                throw new AccessDeniedException(
                        "Un patient ne peut accéder qu'à ses propres consultations.");
            }
        }

        if (role == Role.MEDECIN) {
            String medecinId = SecurityUtils.getCurrentMedecinId(utilisateurRepository);
            if (medecinId == null || !consultationRepository
                    .existsByPatientIdAndMedecinId(patientId, medecinId)) {
                throw new AccessDeniedException("Accès aux consultations de ce patient non autorisé.");
            }
        }

        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException(
                    "Patient introuvable : " + patientId);
        }

        if (role == Role.MEDECIN) {
            return consultationRepository.findByPatientIdAndMedecinId(
                    patientId, SecurityUtils.getCurrentMedecinId(utilisateurRepository), pageable);
        }
        return consultationRepository.findByPatientId(patientId, pageable);
    }

    /**
     * Recherche une consultation par son numéro.
     * <p>
     * L'accès est contrôlé selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : ne peut accéder qu'à ses propres consultations</li>
     * <li>MEDECIN : peut accéder à toutes les consultations</li>
     * <li>ADMIN : peut accéder à toutes les consultations</li>
     * </ul>
     * </p>
     *
     * @param numero le numéro de la consultation
     * @return la consultation trouvée
     * @throws ResourceNotFoundException si la consultation n'existe pas
     * @throws IllegalStateException si l'utilisateur n'a pas les droits d'accès
     */
    public Consultation getConsultationById(String numero) {
        Consultation consultation = consultationRepository.findById(numero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consultation introuvable : " + numero));

        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            String patientId = SecurityUtils.getCurrentPatientId(utilisateurRepository);
            if (patientId == null || !patientId.equals(consultation.getPatientId())) {
                throw new AccessDeniedException(
                        "Un patient ne peut accéder qu'à ses propres consultations.");
            }
        }

        if (role == Role.MEDECIN) {
            String medecinId = SecurityUtils.getCurrentMedecinId(utilisateurRepository);
            if (medecinId == null || !medecinId.equals(consultation.getMedecinId())) {
                throw new AccessDeniedException("Accès à cette consultation non autorisé.");
            }
        }

        return consultation;
    }

    /**
     * Crée une consultation programmée dans le futur.
     *
     * @param consultation la consultation à créer
     * @return la consultation créée
     */
    public Consultation createConsultation(Consultation consultation) {
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);
        if (role == Role.PATIENT) {
            throw new AccessDeniedException("Un patient ne peut pas créer une consultation.");
        }
        String currentMedecinId = role == Role.MEDECIN
                ? SecurityUtils.getCurrentMedecinId(utilisateurRepository) : null;
        if (role == Role.MEDECIN && (currentMedecinId == null
                || !currentMedecinId.equals(consultation.getMedecinId()))) {
            throw new AccessDeniedException("Un médecin ne peut créer que ses propres consultations.");
        }
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
        if (role == Role.MEDECIN) {
            var patient = patientRepository.findById(consultation.getPatientId()).orElseThrow();
            if (!currentMedecinId.equals(patient.getMedecinId())) {
                throw new AccessDeniedException("Le patient n'est pas rattaché à ce médecin.");
            }
        }
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

        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);
        if (role == Role.PATIENT) {
            throw new AccessDeniedException("Un patient ne peut pas modifier une consultation.");
        }

        Consultation existing = getConsultationById(numero);

        if (role == Role.MEDECIN && (!existing.getPatientId().equals(consultation.getPatientId())
                || !existing.getMedecinId().equals(consultation.getMedecinId()))) {
            throw new AccessDeniedException("Un médecin ne peut pas réaffecter une consultation.");
        }

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
        if (SecurityUtils.getCurrentRole(utilisateurRepository) == Role.PATIENT) {
            throw new AccessDeniedException("Un patient ne peut pas supprimer une consultation.");
        }
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

        if (SecurityUtils.getCurrentRole(utilisateurRepository) == Role.PATIENT) {
            throw new AccessDeniedException("Un patient ne peut pas modifier les prescriptions.");
        }

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
