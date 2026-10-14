package fr.dawan.cc_java_mongo.exception;

/**
 * Exception levée lorsqu'une ressource demandée n'existe pas.
 * <p>
 * Cette exception est utilisée dans les services et les repositories pour signaler
 * qu'une entité recherchée (patient, médecin, médicament, consultation, etc.)
 * n'a pas été trouvée dans la base de données.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.exception.GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construit une exception avec un message descriptif.
     *
     * @param message le message décrivant la ressource introuvable
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
