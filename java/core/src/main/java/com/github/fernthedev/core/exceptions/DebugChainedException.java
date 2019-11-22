package com.github.fernthedev.core.exceptions;

public class DebugChainedException extends DebugException {

    private Exception exception;
    private String message;

    public DebugChainedException() {
        new IllegalArgumentException("Use DebugException instead").printStackTrace();
    }

    public DebugChainedException(Exception exception,String message) {
        super(message);
        this.exception = exception;
    }

    @Override
    public void printStackTrace() {
        System.out.println(message);
        super.printStackTrace();
        initCause(exception);
        System.err.println("Caused by: " + getCause());
        getCause().printStackTrace();
    }
}
