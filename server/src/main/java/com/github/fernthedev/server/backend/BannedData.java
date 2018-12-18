package com.github.fernthedev.server.backend;

public class BannedData {
    public BannedData() {}
    public BannedData(String ip) {
        this.ip = ip;
    }

    private String ip;

    public String getIp() {
        return ip;
    }
}