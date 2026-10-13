package fr.dawan.cc_java_mongo.exception;

/**
 * Exception levée lorsqu'une ressource demandée n'existe pas.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
