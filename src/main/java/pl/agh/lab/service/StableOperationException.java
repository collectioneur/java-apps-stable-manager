package pl.agh.lab.service;

public class StableOperationException extends Exception {

    public StableOperationException(String message) {
        super(message);
    }

    public StableOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}