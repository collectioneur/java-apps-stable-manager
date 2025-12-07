package pl.agh.lab.service;

public class HorseOperationException extends Exception {

    public HorseOperationException(String message) {
        super(message);
    }

    public HorseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}