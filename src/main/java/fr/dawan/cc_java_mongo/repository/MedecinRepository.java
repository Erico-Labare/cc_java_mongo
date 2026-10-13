
package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Medecin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Fournit les opérations d'accès aux médecins dans MongoDB.
 */
public interface MedecinRepository
        extends MongoRepository<Medecin, String> {

    /**
     * Recherche les médecins dont le nom contient le texte fourni,
     * sans tenir compte de la casse.
     *
     * @param nom texte à rechercher
     * @param pageable paramètres de pagination
     * @return page de médecins correspondants
     */
    Page<Medecin> findByNomContainingIgnoreCase(
            String nom,
            Pageable pageable);
}
