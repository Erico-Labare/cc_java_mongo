
package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository Spring Data pour l'accès aux patients dans MongoDB.
 * <p>
 * Cette interface étend {@link MongoRepository} pour fournir les opérations
 * CRUD standard sur les patients, ainsi qu'une recherche par nom.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Patient
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
public interface PatientRepository
        extends MongoRepository<Patient, String> {

    java.util.List<Patient> findByMedecinId(String medecinId);

    /**
     * Recherche les patients dont le nom contient le texte fourni,
     * sans tenir compte de la casse.
     *
     * @param nom texte à rechercher dans les noms de patients
     * @param pageable paramètres de pagination et de tri
     * @return page de patients correspondants au critère de recherche
     */
    Page<Patient> findByNomContainingIgnoreCase(
            String nom,
            Pageable pageable);
}
