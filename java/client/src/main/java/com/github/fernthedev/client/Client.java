package com.github.fernthedev.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {
    public boolean registered;
    protected Scanner scanner;
    public boolean running = false;


    protected static Logger logger;

    public int port;
    public String host;

    @Getter
    @Setter
    @NonNull
    protected String serverKey;

    @Getter
    @Setter
    @NonNull
    protected String privateKey;



    @Getter
    protected UUID uuid;

    public String name;
    public static WaitForCommand waitForCommand;
    public static Thread waitThread;

    public static Thread currentThread;

    protected ClientThread clientThread;

    protected boolean closeConsole = true;

    public boolean isCloseConsole() {
        return closeConsole;
    }

    /*
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        logger = Logger.getLogger(Client.class.getName());
    }*/


    public Client(String host, int port) {
        this.port = port;
        this.host = host;
        this.scanner = Main.scanner;

        logger = Logger.getLogger(Client.class.getName());

        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, e.getMessage(), e.getCause());
            clientThread.close();
        }

        clientThread = new ClientThread(this);

        waitForCommand = new WaitForCommand(this);



        currentThread = new Thread(clientThread,"MainThread");

    }

    protected void getProperties() {
        System.getProperties().list(System.out);
    }

    public void initialize() {
        logger.info("Initializing");
        name = null;
        clientThread.connected = false;
        clientThread.connectToServer = true;
        clientThread.running = true;



        clientThread.connect();

    }

    public String getOSName() {
        return System.getProperty("os.name");
    }

    public static synchronized Logger getLogger() {
        if(logger == null) {
            logger = Logger.getLogger(Client.class.getName());
        }

        return logger;
    }

    public ClientThread getClientThread() {
        return clientThread;
    }
}
