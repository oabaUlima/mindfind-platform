package pe.edu.ulima.mindfind.shared.application.exceptions;

/** Thrown by application services when an aggregate cannot be found. Translated to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
