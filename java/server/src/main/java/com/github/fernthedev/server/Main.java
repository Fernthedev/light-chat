package com.github.fernthedev.server;

import com.github.fernthedev.universal.StaticHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner scanner;

    public static void main(String[] args) {
        new StaticHandler();
        scanner = new Scanner(System.in);
        Logger.getLogger("io.netty").setLevel(Level.OFF);

        int port = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equalsIgnoreCase("-port")) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    port = -1;
                }
            }

            if(arg.equalsIgnoreCase("-lightmanager")) {
                StaticHandler.isLight = true;
            }

            if (arg.equalsIgnoreCase("-debug")) {
                StaticHandler.isDebug = true;
            }
        }

        if(StaticHandler.isDebug) Server.getLogger().setLevel(Level.DEBUG);
        else Server.getLogger().setLevel(Level.INFO);

        if (port == -1) port = 2000;

        if(System.console() == null && !StaticHandler.isDebug) {

            String filename = Main.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            Server.getLogger().info("No console found. Try using -debug arg");

            String[] newArgs = new String[]{"cmd","/c","start","cmd","/c","java -jar -Xmx2G -Xms2G \"" + filename + "\""};

            List<String> launchArgs = new ArrayList<>(Arrays.asList(newArgs));
            launchArgs.addAll(Arrays.asList(args));

            try {
                Runtime.getRuntime().exec(launchArgs.toArray(new String[]{}));
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Server server = new Server(port);
        new Thread(server).start();
    }

    public static String readLine(String message) {
        Server.getLogger().info(message + "\n>");
        if (scanner.hasNextLine())
            return scanner.nextLine();
        else return null;
    }

    public static int readInt(String message) {
        Server.getLogger().info(message + "\n>");
        if (scanner.hasNextLine())
            return scanner.nextInt();
        else return -1;
    }
}
