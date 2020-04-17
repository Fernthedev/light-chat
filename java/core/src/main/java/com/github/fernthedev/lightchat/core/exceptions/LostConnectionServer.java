package com.github.fernthedev.lightchat.core.exceptions;

public class LostConnectionServer extends Exception {

    private String ip;

    public LostConnectionServer(String ip) {
        this.ip = ip;
    }



}
