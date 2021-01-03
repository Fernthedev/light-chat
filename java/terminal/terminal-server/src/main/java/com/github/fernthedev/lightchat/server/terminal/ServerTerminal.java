package com.github.fernthedev.lightchat.server.terminal;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.github.fernthedev.fernutils.console.ArgumentArrayUtils;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.server.Console;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.settings.ServerSettings;
import com.github.fernthedev.lightchat.server.terminal.backend.AutoCompleteHandler;
import com.github.fernthedev.lightchat.server.terminal.backend.TabCompleteFinder;
import com.github.fernthedev.lightchat.server.terminal.command.AuthTerminalHandler;
import com.github.fernthedev.lightchat.server.terminal.command.Command;
import com.github.fernthedev.lightchat.server.terminal.command.SettingsCommand;
import com.github.fernthedev.terminal.core.CommonUtil;
import com.github.fernthedev.terminal.core.ConsoleHandler;
import com.github.fernthedev.terminal.core.TermCore;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerTerminal {

    @Getter
    private static Config<? extends ServerSettings> settingsManager;

    @Getter
    private static ServerCommandHandler commandHandler;

    protected static Server server;

    @Getter
    private static CommandMessageParser commandMessageParser;

    @Getter
    private static TabCompleteFinder autoCompleteHandler;

    @Getter
    private static List<Command> commandList = new ArrayList<>();

    protected static Logger logger = LoggerFactory.getLogger(ServerTerminal.class);

    public static void main(String[] args) {

        AtomicInteger port = new AtomicInteger(-1);

        ArgumentArrayUtils.parseArguments(args)
                .handle("-port", queue -> {

                    try {
                        port.set(Integer.parseInt(queue.remove()));
                        if (port.get() <= 0) {
                            logger.error("-port cannot be less than 0");
                            port.set(-1);
                        } else logger.info("Using port {}", port);
                    } catch (NumberFormatException e) {
                        logger.error("-port is not a number");
                        port.set(-1);
                    }



                })
                .handle("-debug", queue -> StaticHandler.setDebug(true))
                .apply();

        init(args,
                ServerTerminalSettings.builder()
                .port(port.get())
                .build());
        startBind();
    }

    public static void init(ServerTerminalSettings terminalSettings) {
        init(new String[0], terminalSettings);
    }

    public static void init(String[] args, ServerTerminalSettings terminalSettings) {
        CommonUtil.initTerminal();


        //  Logger.getLogger("io.netty").setLevel(java.util.logging.Level.OFF);


        int port = terminalSettings.port;


        if (terminalSettings.isLaunchConsoleInCMDWhenNone())
            CommonUtil.startSelfInCmd(args);


        try {
            settingsManager = terminalSettings.getServerSettings();
            settingsManager.load();
            settingsManager.save();
        } catch (ConfigLoadException e) {
            e.printStackTrace();
        }


        if (port == -1) port = settingsManager.getConfigData().getPort();


        server = new Server(port);
        server.addPacketHandler(new TerminalPacketHandler(server));
        server.setSettingsManager(settingsManager);
        commandHandler = new ServerCommandHandler(server);



        StaticHandler.setCore(new ServerTermCore(server), true);

        if (terminalSettings.isAllowTermPackets())
            CommonUtil.registerTerminalPackets();

        if (terminalSettings.isConsoleCommandHandler()) {
            server.getStartupLock().thenRun(() -> {
                ConsoleHandler.startConsoleHandlerAsync((TermCore) StaticHandler.getCore(), new AutoCompleteHandler(server));
                logger.info("Command handler ready! Type Command: (try help)");
            });
        }

        commandMessageParser = new CommandMessageParser(server);
        server.getPluginManager().registerEvents(commandMessageParser);

        registerCommand(new SettingsCommand(server));

        if (terminalSettings.isAllowChangePassword())
            registerCommand(new AuthTerminalHandler("changepassword", server));

        autoCompleteHandler = new TabCompleteFinder(server);
    }

    public static void startBind() {
        new Thread(() -> {

            // Run on startup
            server.getStartupLock().thenRunAsync(() -> {
                try {
                    if (!server.bind().await(15, TimeUnit.SECONDS)) server.getLogger().error("Unable to bind port. Took longer than 15 seconds");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            });

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
