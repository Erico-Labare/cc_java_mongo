
package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Medecin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository Spring Data pour l'accès aux médecins dans MongoDB.
 * <p>
 * Cette interface étend {@link MongoRepository} pour fournir les opérations
 * CRUD standard sur les médecins, ainsi qu'une recherche par nom.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Medecin
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
public interface MedecinRepository
        extends MongoRepository<Medecin, String> {

    /**
     * Recherche les médecins dont le nom contient le texte fourni,
     * sans tenir compte de la casse.
     *
     * @param nom texte à rechercher dans les noms de médecins
     * @param pageable paramètres de pagination et de tri
     * @return page de médecins correspondants au critère de recherche
     */
    Page<Medecin> findByNomContainingIgnoreCase(
            String nom,
            Pageable pageable);
}
