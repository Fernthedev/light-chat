package com.github.fernthedev.server;

import com.github.fernthedev.core.StaticHandler;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {

    static Scanner scanner;

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        new StaticHandler();
        Logger.getLogger("io.netty").setLevel(java.util.logging.Level.OFF);
        StaticHandler.setupLoggers();


      //  Logger.getLogger("io.netty").setLevel(java.util.logging.Level.OFF);

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


        if (port == -1) port = 2000;

        if (System.console() == null && !StaticHandler.isDebug) {

            String filename = Main.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            System.out.println("No console found");

            String[] newArgs = new String[]{"cmd", "/c", "start", "cmd", "/c", "java -jar -Xmx2G -Xms2G \"" + filename + "\""};

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

        StaticHandler.setCore(new ServerCore(server));
        new Thread(server,"ServerMainThread").start();
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
