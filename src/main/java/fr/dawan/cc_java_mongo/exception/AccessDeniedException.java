package fr.dawan.cc_java_mongo.exception;

/** Exception métier utilisée lorsqu'un utilisateur authentifié n'a pas accès à une ressource. */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
