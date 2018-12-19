package com.github.fernthedev.client;

import com.github.fernthedev.exceptions.LostConnectionServer;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
    public boolean registered;
    private Scanner scanner;
    boolean running = false;



    private static final Logger logger = Logger.getLogger(Client.class);

     int port;
     String host;

    public String name;
    static waitForCommand WaitForCommand;
    static Thread waitThread;

    static Thread currentThread;

    private ClientThread clientThread;

    /*
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        logger = Logger.getLogger(Client.class.getName());
    }*/


    Client(String host, int port) {
        this.port = port;
        this.host = host;
        this.scanner = Main.scanner;
        name = null;
        WaitForCommand = new waitForCommand(this);

        clientThread = new ClientThread(this);

        currentThread = new Thread(clientThread);

    }

    void initialize() {
        logger.info("Initializing");
        name = null;
        clientThread.connected = false;
        clientThread.connectToServer = true;
        clientThread.running = true;



        /*
        if(!registered) {
            logger.info("Type in your desired username:");
            while (!registered || name == null) {
                    name = Main.readLine("");

                    if(name == null || name.equals("")) {
                        registered = false;
                        name = null;
                    }
                    else
                    registered = true;
            }
        }*/



        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            getLogger().error(e.getMessage(),e.getCause());
            clientThread.close();
        }

        while(!clientThread.connected && clientThread.connectToServer) {
            clientThread.connect();

        }
    }






    void throwException() {
        try {
            throw new LostConnectionServer(host);
        } catch (LostConnectionServer lostConnectionServer) {
            lostConnectionServer.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public ClientThread getClientThread() {
        return clientThread;
    }
}
