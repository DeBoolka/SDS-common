package ru.mirea.dikanev.nikita.common.server.exception;

public class HandlerInternalException extends RuntimeException {

    public HandlerInternalException(String message) {
        super(message);
    }

    public HandlerInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerInternalException(Throwable cause) {
        super(cause);
    }
}
