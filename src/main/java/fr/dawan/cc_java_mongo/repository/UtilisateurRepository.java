package fr.dawan.cc_java_mongo.repository;

import fr.dawan.cc_java_mongo.model.Utilisateur;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository Spring Data pour l'accès aux utilisateurs dans MongoDB.
 * <p>
 * Cette interface étend {@link MongoRepository} pour fournir les opérations
 * CRUD standard sur les utilisateurs, ainsi qu'une recherche par nom d'utilisateur.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.model.Utilisateur
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
public interface UtilisateurRepository
        extends MongoRepository<Utilisateur, String> {

    /**
     * Recherche un utilisateur par son nom d'utilisateur.
     *
     * @param username le nom d'utilisateur recherché
     * @return un Optional contenant l'utilisateur trouvé, ou vide s'il n'existe pas
     */
    Optional<Utilisateur> findByUsername(String username);
}
