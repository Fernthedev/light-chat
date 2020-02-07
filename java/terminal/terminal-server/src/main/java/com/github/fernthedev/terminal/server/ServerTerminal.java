package com.github.fernthedev.terminal.server;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.gson.GsonConfig;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.ServerCore;
import com.github.fernthedev.server.command.SettingsCommand;
import com.github.fernthedev.server.settings.Settings;
import com.github.fernthedev.terminal.core.ConsoleHandler;
import lombok.Getter;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerTerminal {

    @Getter
    private static Config<Settings> settingsManager;

    private static Logger logger = LoggerFactory.getLogger(ServerTerminal.class);

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        java.util.logging.Logger.getLogger("io.netty").setLevel(java.util.logging.Level.OFF);
        StaticHandler.setupLoggers();


        //  Logger.getLogger("io.netty").setLevel(java.util.logging.Level.OFF);

        int port = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equalsIgnoreCase("-port")) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                    if (port < 0) {
                        logger.error("-port cannot be less than 0");
                        port = -1;
                    } else logger.info("Using port {}", args[i + +1]);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    logger.error("-port is not a number");
                    port = -1;
                }
            }

            if (arg.equalsIgnoreCase("-lightmanager")) {
                StaticHandler.setLight(true);
            }

            if (arg.equalsIgnoreCase("-debug")) {
                StaticHandler.setDebug(true);
                logger.debug("Debug enabled");
            }
        }


        if (System.console() == null && !StaticHandler.isDebug()) {

            String filename = Server.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            logger.warn("No console found");

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


        settingsManager = new GsonConfig<>(new Settings(), new File(getCurrentPath(), "settings.json"));
        settingsManager.save();

        port = settingsManager.getConfigData().getPort();


        Server server = new Server(port);
        server.setSettingsManager(settingsManager);

        StaticHandler.setCore(new ServerCore(server));
        new Thread(() -> {
            logger.info("Type Command: (try help)");
            new ConsoleHandler(new AutoCompleteHandler(server)).start();
        }, "ConsoleHandler").start();
        server.registerCommand(new SettingsCommand(server));

        new Thread(server, "ServerMainThread").start();
    }

    public static String getCurrentPath() {
        return Paths.get("").toAbsolutePath().toString();
    }
}
