package com.github.fernthedev.client;

public class ServerAddress {

    private String address;
    private int port;
    private String version;

    private int clientNumbers = 0;

    public ServerAddress(String address,int port,String version) {
        this.address = address;
        this.port = port;
        this.version = version;
    }

    public int getClientNumbers() {
        return clientNumbers;
    }

    public void setClientNumbers(int clientNumbers) {
        this.clientNumbers = clientNumbers;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getVersion() {
        return version;
    }
}
