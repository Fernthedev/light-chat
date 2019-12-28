package com.github.fernthedev.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MulticastData {

    public MulticastData() {}

    public MulticastData(int port, String verison, String minVersion) {
        this.port = port;
        this.version = verison;
        this.minVersion = minVersion;
        this.address = address;
    }

    public MulticastData(int port, String version, String minVersion, int clientNumbers) {
        this(port, version, minVersion);
        this.clientNumbers = clientNumbers;
    }

    private String address;

    private String version;
    private String minVersion;

    private int port;

    private int clientNumbers = 0;
    private List<String> clients =  new ArrayList<>();



}
