package com.github.fernthedev.server;

import com.github.fernthedev.universal.StaticHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner scanner;

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        new StaticHandler();
        scanner = new Scanner(System.in);

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

        Level level;
        if(StaticHandler.isDebug) level = Level.DEBUG;
         else level = Level.INFO;



        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(Server.getLogger().getName());
        loggerConfig.setLevel(level);
        ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.

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
