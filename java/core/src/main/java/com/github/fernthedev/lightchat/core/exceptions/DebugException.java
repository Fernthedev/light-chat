package com.github.fernthedev.lightchat.core.exceptions;

public class DebugException extends RuntimeException {

    public DebugException() { }

    public DebugException(String message) {
        super(message);
    }

    public DebugException(Throwable throwable) {
        super(throwable);
    }
}
