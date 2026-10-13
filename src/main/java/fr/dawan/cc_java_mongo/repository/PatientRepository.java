
package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Fournit les opérations d'accès aux patients dans MongoDB.
 */
public interface PatientRepository
        extends MongoRepository<Patient, String> {

    /**
     * Recherche les patients dont le nom contient le texte fourni,
     * sans tenir compte de la casse.
     *
     * @param nom texte à rechercher
     * @param pageable paramètres de pagination
     * @return page de patients correspondants
     */
    Page<Patient> findByNomContainingIgnoreCase(
            String nom,
            Pageable pageable);
}
