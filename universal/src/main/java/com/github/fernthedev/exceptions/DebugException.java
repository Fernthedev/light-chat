package com.github.fernthedev.exceptions;

public class DebugException extends Exception {

    public DebugException() { }

    public DebugException(String message) {
        super(message);
    }

    public DebugException(Throwable throwable) {
        super(throwable);
    }
}
