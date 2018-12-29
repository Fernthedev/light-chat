package com.github.fernthedev.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {
    public boolean registered;
    protected Scanner scanner;
    public boolean running = false;


    protected static final Logger logger = Logger.getLogger(Client.class.getName());

     public int port;
     public String host;

    public String name;
    public static com.github.fernthedev.client.WaitForCommand WaitForCommand;
    public static Thread waitThread;

    public static Thread currentThread;

    protected ClientThread clientThread;

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
        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING,e.getMessage(),e.getCause());
            clientThread.close();
        }
        WaitForCommand = new WaitForCommand(this);

        clientThread = new ClientThread(this);

        currentThread = new Thread(clientThread);
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

    public static Logger getLogger() {
        return logger;
    }

    public ClientThread getClientThread() {
        return clientThread;
    }
}
