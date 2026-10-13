
package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Consultation;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Fournit les opérations d'accès aux consultations dans MongoDB.
 */
public interface ConsultationRepository
        extends MongoRepository<Consultation, String> {

    /**
     * Recherche les consultations d'un patient, avec pagination.
     *
     * @param patientId identifiant du patient
     * @param pageable paramètres de pagination
     * @return page de consultations du patient
     */
    Page<Consultation> findByPatientId(
            String patientId,
            Pageable pageable);

    /**
     * Recherche les consultations dont la date est postérieure
     * à la date fournie, avec pagination.
     *
     * @param date date et heure de référence
     * @param pageable paramètres de pagination
     * @return page de consultations futures
     */
    Page<Consultation> findByDateAfter(
            LocalDateTime date,
            Pageable pageable);
}
