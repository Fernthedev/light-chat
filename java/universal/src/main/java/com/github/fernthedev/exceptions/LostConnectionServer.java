package com.github.fernthedev.exceptions;

public class LostConnectionServer extends Exception {

    private String ip;

    public LostConnectionServer(String ip) {
        this.ip = ip;
    }



}
