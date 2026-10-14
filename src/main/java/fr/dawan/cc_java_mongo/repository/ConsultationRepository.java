
package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Consultation;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository Spring Data pour l'accès aux consultations dans MongoDB.
 * <p>
 * Cette interface étend {@link MongoRepository} pour fournir les opérations
 * CRUD standard sur les consultations, ainsi que des recherches par patient
 * et par date.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Consultation
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
public interface ConsultationRepository
        extends MongoRepository<Consultation, String> {

    /**
     * Recherche les consultations d'un patient, avec pagination.
     *
     * @param patientId identifiant du patient (numéro de Sécurité sociale)
     * @param pageable paramètres de pagination et de tri
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
     * @param pageable paramètres de pagination et de tri
     * @return page de consultations futures
     */
    Page<Consultation> findByDateAfter(
            LocalDateTime date,
            Pageable pageable);
}
