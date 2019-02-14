package com.github.fernthedev.universal;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MulticastData {

    public MulticastData() {}

    public MulticastData(int port, String verison) {
        this.port = port;
        this.version = verison;
    }

    public MulticastData(int port, String verison,int clientNumbers) {
        this(port,verison);
        this.clientNumbers = clientNumbers;
    }

    private String address;

    private String version;
    private int port;

    private int clientNumbers = 0;
    private List<String> clients =  new ArrayList<>();



}
