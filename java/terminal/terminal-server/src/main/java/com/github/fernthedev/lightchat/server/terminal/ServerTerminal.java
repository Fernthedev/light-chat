package com.github.fernthedev.lightchat.server.terminal;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.github.fernthedev.config.gson.GsonConfig;
import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.light.LightManager;
import com.github.fernthedev.light.exceptions.NoPi4JLibsFoundException;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.server.security.AuthenticationManager;
import com.github.fernthedev.lightchat.server.Console;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.settings.ServerSettings;
import com.github.fernthedev.lightchat.server.terminal.backend.AutoCompleteHandler;
import com.github.fernthedev.lightchat.server.terminal.backend.TabCompleteFinder;
import com.github.fernthedev.lightchat.server.terminal.command.AuthTerminalHandler;
import com.github.fernthedev.lightchat.server.terminal.command.Command;
import com.github.fernthedev.lightchat.server.terminal.command.LightCommand;
import com.github.fernthedev.lightchat.server.terminal.command.SettingsCommand;
import com.github.fernthedev.terminal.core.CommonUtil;
import com.github.fernthedev.terminal.core.ConsoleHandler;
import com.github.fernthedev.terminal.core.TermCore;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServerTerminal {

    @Getter
    private static Config<ServerSettings> settingsManager;

    @Getter
    private static ServerCommandHandler commandHandler;

    protected static Server server;



    @Getter
    private static CommandMessageParser commandMessageParser;


    @Getter
    private static AuthenticationManager authenticationManager;

    @Getter
    private static TabCompleteFinder autoCompleteHandler;


    @Getter
    private static List<Command> commandList = new ArrayList<>();


    private static Logger logger = LoggerFactory.getLogger(ServerTerminal.class);



    public static void main(String[] args) {
        init(args, ServerTerminalSettings.builder().build());
        startBind();
    }

    public static void init(ServerTerminalSettings terminalSettings) {
        init(new String[0], terminalSettings);
    }

    public static void init(String[] args, ServerTerminalSettings terminalSettings) {
        CommonUtil.initTerminal();


        //  Logger.getLogger("io.netty").setLevel(java.util.logging.Level.OFF);


        int port = terminalSettings.port;

        for (int i = 0; i < args.length && (terminalSettings.isAllowPortArgParse() || terminalSettings.isAllowDebugArgParse() || terminalSettings.isLightAllowed()); i++) {
            String arg = args[i];

            if (arg.equalsIgnoreCase("-port") && terminalSettings.isAllowPortArgParse()) {
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

            if (arg.equalsIgnoreCase("-lightmanager") && terminalSettings.isLightAllowed()) {
                StaticHandler.setLight(true);
            }

            if (arg.equalsIgnoreCase("-debug") && terminalSettings.isAllowDebugArgParse()) {
                StaticHandler.setDebug(true);
                logger.debug("Debug enabled");
            }
        }


        if (terminalSettings.isLaunchConsoleWhenNull())
            CommonUtil.startSelfInCmd(args);


        try {
            settingsManager = new GsonConfig<>(terminalSettings.getServerSettings(), new File(getCurrentPath(), "settings.json"));
            settingsManager.save();
        } catch (ConfigLoadException e) {
            e.printStackTrace();
        }


        if (port == -1) port = settingsManager.getConfigData().getPort();


        server = new Server(port);
        server.addPacketHandler(new TerminalPacketHandler(server));
        server.setSettingsManager(settingsManager);
        commandHandler = new ServerCommandHandler(server);



        StaticHandler.setCore(new ServerTermCore(server));

        if (terminalSettings.isAllowTermPackets())
            CommonUtil.registerTerminalPackets();

        if (terminalSettings.isConsoleCommandHandler()) {
            new Thread(() -> {
                logger.info("Type Command: (try help)");
                new ConsoleHandler((TermCore) StaticHandler.getCore(), new AutoCompleteHandler(server)).start();
            }, "ConsoleHandler").start();
        }

        commandMessageParser = new CommandMessageParser(server);
        server.getPluginManager().registerEvents(commandMessageParser);

        registerCommand(new SettingsCommand(server));

        ThreadUtils.runAsync(() -> {
            authenticationManager = new AuthenticationManager(server);
            server.getPluginManager().registerEvents(authenticationManager);
        }, server.getExecutorService());

        if (terminalSettings.isAllowChangePassword())
            registerCommand(new AuthTerminalHandler("changepassword", server));

        autoCompleteHandler = new TabCompleteFinder(server);

        if (terminalSettings.isLightAllowed())
            ThreadUtils.runAsync(() -> {
                if (StaticHandler.OS.equalsIgnoreCase("Linux") || StaticHandler.OS.contains("Linux") || StaticHandler.isLight()) {
                    logger.info("Running LightManager (Note this is for raspberry pies only)");

                    Thread lightThread = new Thread(() -> {

                        try {
                            LightManager.init();
                            registerCommand(new LightCommand(server));
                        } catch (IllegalArgumentException | ExceptionInInitializerError | NoPi4JLibsFoundException e) {
                            logger.error("Unable to load Pi4J Libraries. To load stacktrace, add -debug flag. Message: {}", e.getMessage());
                            if (StaticHandler.isDebug()) {
                                e.printStackTrace();
                                registerCommand(new LightCommand(server));
                            }
                        }

                    }, "LightThread");
//                registerCommand(new LightCommand(this));
                    lightThread.start();
                } else {
                    logger.info("Detected system is not linux. LightManager will not run (manual run with -lightmanager arg)");
                }
            }, server.getExecutorService());
    }

    public static void startBind() {
        new Thread(() -> {
            // Run on startup
            server.getStartupLock().thenRunAsync(() -> server.bind());

            server.run();

        }, "ServerMainStartupThread").start();
    }

    public static Command registerCommand(Command command) {
        commandList.add(command);
        return command;
    }

    public static List<Command> getCommands() {
        return commandList;
    }

    public static String getCurrentPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    public static void broadcast(String message) {
        server.getLogger().info(message);
        server.sendObjectToAllPlayers(new MessagePacket(message));
    }

    public static void sendMessage(SenderInterface senderInterface, String message) {
        if (senderInterface instanceof Console) logger.info(message);
        else senderInterface.sendPacket(new MessagePacket(message));
    }
}
