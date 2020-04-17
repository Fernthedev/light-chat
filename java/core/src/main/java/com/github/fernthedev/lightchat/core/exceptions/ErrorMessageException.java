package com.github.fernthedev.lightchat.core.exceptions;

public class ErrorMessageException extends Exception {

    public ErrorMessageException(String string) {
        super(string);
    }

    public ErrorMessageException(Exception ex) {
        super(ex);
    }

    public ErrorMessageException(String s,Exception ex) {
        super(s,ex);
  