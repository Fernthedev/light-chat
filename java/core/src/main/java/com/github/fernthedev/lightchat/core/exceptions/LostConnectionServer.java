package com.github.fernthedev.core.exceptions;

public class LostConnectionServer extends Exception {

    private String ip;

    public LostConnectionServer(String ip) {
        this.ip = ip;
    }



}
