package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Utilisateur;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Accès aux utilisateurs enregistrés dans MongoDB.
 */
public interface UtilisateurRepository
        extends MongoRepository<Utilisateur, String> {

    Optional<Utilisateur> findByUsername(String username);
}
