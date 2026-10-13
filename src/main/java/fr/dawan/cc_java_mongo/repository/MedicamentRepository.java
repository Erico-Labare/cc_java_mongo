package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Medicament;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Fournit les opérations d'accès au catalogue des médicaments.
 */
public interface MedicamentRepository
        extends MongoRepository<Medicament, String> {
}
