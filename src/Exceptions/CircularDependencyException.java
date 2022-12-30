package Exceptions;

/**
 * Signals that a circular dependency has occurred.
 */
public class CircularDependencyException extends Exception {
    /**
     * Constructs a {@link CircularDependencyException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public CircularDependencyException(String message) {
        super(message);
    }
}
