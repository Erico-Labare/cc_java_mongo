package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Medicament;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository Spring Data pour l'accès au catalogue des médicaments.
 * <p>
 * Cette interface étend {@link MongoRepository} pour fournir les opérations
 * CRUD standard sur les médicaments.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Medicament
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
public interface MedicamentRepository
        extends MongoRepository<Medicament, String> {
}
