package com.github.fernthedev.client;

import com.github.fernthedev.client.backend.AutoCompleteHandler;
import com.github.fernthedev.universal.StaticHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.UUID;


public class Client {
    public boolean registered;
    protected Scanner scanner;
    public boolean running = false;


    private static Logger logger;

    @Getter
    protected IOSCheck osCheck;

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
    private static CLogger cLogger;

    public static Thread currentThread;

    protected ClientThread clientThread;

    protected boolean closeConsole = true;

    public boolean isCloseConsole() {
        return closeConsole;
    }


    public Client(String host, int port) {
        this.port = port;
        this.host = host;
        this.scanner = Main.scanner;

        registerOSCheck();
        registerLogger();

        StaticHandler.setupTerminal(new AutoCompleteHandler(this));


        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            getLogger().logError(e.getMessage(), e.getCause());
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
        getLogger().info("Initializing");
        name = null;
        clientThread.connected = false;
        clientThread.connectToServer = true;
        clientThread.running = true;



        clientThread.connect();

    }

    protected void registerOSCheck() {
        osCheck = new DesktopOSCheck();
    }

    protected void registerLogger() {
        logger = LogManager.getLogger(Client.class.getName());
        cLogger = new CLogger(logger);
    }

    public String getOSName() {
        return System.getProperty("os.name");
    }

    public ILogManager getLogger() {
        return cLogger;
    }

    public ClientThread getClientThread() {
        return clientThread;
    }
}
