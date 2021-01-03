package com.github.fernthedev.lightchat.client.terminal;

import com.github.fernthedev.fernutils.console.ArgumentArrayUtils;
import com.github.fernthedev.lightchat.client.Client;
import com.github.fernthedev.lightchat.client.netty.MulticastClient;
import com.github.fernthedev.lightchat.core.MulticastData;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.VersionData;
import com.github.fernthedev.terminal.core.CommonUtil;
import com.github.fernthedev.terminal.core.ConsoleHandler;
import com.github.fernthedev.terminal.core.TermCore;
import com.github.fernthedev.terminal.core.packets.CommandPacket;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class ClientTerminal {

    @Getter
    protected static Logger logger = LoggerFactory.getLogger(ClientTerminal.class);

    @Getter
    private static AutoCompleteHandler autoCompleteHandler;
    protected static Client client;

    protected static BiFunction<String, Integer, ? extends Client> clientSupplier = Client::new;

    @Getter
    private static final Stopwatch messageDelay = Stopwatch.createUnstarted();

    public static void main(String[] args) {
        AtomicInteger port = new AtomicInteger(-1);
        AtomicReference<String> host = new AtomicReference<>(null);

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
                .handle("-host", queue -> {
                    try {
                        host.set(queue.remove());
                        logger.info("Using host {}", host.get());
                    } catch (IndexOutOfBoundsException e) {
                        logger.error("Cannot find argument for -host");
                        host.set(null);
                    }
                })
                .handle("-debug", queue -> StaticHandler.setDebug(true))
                .apply();

        init(args, ClientTerminalSettings.builder()
                .host(host.get())
                .port(port.get())
                .build());

        connect();
    }


    public static void init(ClientTerminalSettings settings) {
        init(new String[0], settings);
    }

    @SneakyThrows
    public static void init(String[] args, ClientTerminalSettings settings) {
        CommonUtil.initTerminal();

        final AtomicReference<String> host = new AtomicReference<>(settings.getHost());
        final AtomicInteger port = new AtomicInteger(settings.getPort());


        if (settings.isLaunchConsoleInCMDWhenNone())
            CommonUtil.startSelfInCmd(args);

        if (settings.isCheckForServersInMulticast()) {
            MulticastClient multicastClient;
            Scanner scanner = new Scanner(System.in);
            if (host.get() == null || host.equals("") || port.get() == -1) {
                multicastClient = new MulticastClient();

                try {
                    Pair<String, Integer> hostPortPair = check(multicastClient, scanner, 4);

                    if (hostPortPair != null) {
                        host.set(hostPortPair.getLeft());
                        port.set(hostPortPair.getRight());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (settings.isAskUserForHostPort()) {
            Scanner scanner = new Scanner(System.in);
            while (host.get() == null || host.get().equalsIgnoreCase("") || port.get() == -1) {
                if (host.get() == null || host.get().equals("")) {
                    host.set(readLine(scanner, "Host:"));
                }

                if (port.get() == -1) {
                    port.set(readInt(scanner, "Port:"));
                }
            }
        }

        if (host.get() == null || host.get().equals("")) {
            throw new IllegalStateException("Host is null or not provided. Provide in settings or allow user to provide");
        }

        if (port.get() == -1) {
            throw new IllegalStateException("Port is null or not provided. Provide in settings or allow user to provide");
        }


        client = clientSupplier.apply(host.get(), port.get());

        client.setClientSettingsManager(settings.getClientSettings());

        client.getClientSettingsManager().load();
        client.getClientSettingsManager().save();

        StaticHandler.setCore(new ClientTermCore(client), true);

        if (settings.isAllowTermPackets())
            CommonUtil.registerTerminalPackets();

        if (settings.isConsoleCommandHandler()) {
            autoCompleteHandler = new AutoCompleteHandler(client);

            ConsoleHandler.startConsoleHandlerAsync((TermCore) StaticHandler.getCore(), autoCompleteHandler);
        }

        PacketHandler packetHandler = new PacketHandler();
        client.addPacketHandler(packetHandler);
        client.getPluginManager().registerEvents(packetHandler);



    }

    public static void connect() {
        try {
            client.connect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String readLine(Scanner scanner, String message) {
        if (!(message == null || message.equals(""))) {
            logger.info(message);
        }
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        } else return null;
    }

    private static int readInt(Scanner scanner, String message) {
        if (!(message == null || message.equals(""))) {
            logger.info(message);
        }
        if (scanner.hasNextLine()) {
            return scanner.nextInt();
        } else return -1;
    }

    protected static Pair<String, Integer> check(MulticastClient multicastClient, Scanner scanner, int amount) {
        logger.info("Looking for MultiCast servers");
        multicastClient.checkServers(amount);

        String host = null;
        int port = -1;

        if (!multicastClient.getServersAddress().isEmpty()) {
            Map<Integer, MulticastData> servers = new HashMap<>();
            logger.info("Select one of these servers, or use none to skip, refresh to refresh");
            int index = 0;
            for (MulticastData serverAddress : multicastClient.getServersAddress()) {
                index++;
                servers.put(index, serverAddress);

                DefaultArtifactVersion serverCurrent = new DefaultArtifactVersion(serverAddress.getVersion());
                DefaultArtifactVersion serverMin = new DefaultArtifactVersion(serverAddress.getMinVersion());

                StaticHandler.VersionRange range = StaticHandler.getVersionRangeStatus(new VersionData(serverCurrent, serverMin));

                if (range == StaticHandler.VersionRange.MATCH_REQUIREMENTS) {
                    System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort());
                } else {
                    // Current version is smaller than the server's required minimum
                    if (range == StaticHandler.VersionRange.WE_ARE_LOWER) {
                        System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort() + " (Server's required minimum version is " + serverAddress.getMinVersion() + " while your current version is smaller {" + StaticHandler.getVERSION_DATA().getVersion() + "} Incompatibility issues may arise)");
                    }

                    // Current version is larger than server's minimum version
                    if (range == StaticHandler.VersionRange.WE_ARE_HIGHER) {
                        System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort() + " (Server's version is " + serverAddress.getVersion() + " while your minimum version is larger {" + StaticHandler.getVERSION_DATA().getMinVersion() + "} Incompatibility issues may arise)");
                    }

                }
            }

            while (scanner.hasNextLine()) {
                String answer = scanner.nextLine();

                answer = answer.replaceAll(" ", "");

                if (answer.matches("[0-9]+")) {
                    try {
                        int serverIndex = Integer.parseInt(answer);

                        if (servers.containsKey(serverIndex)) {
                            MulticastData serverAddress = servers.get(index);

                            host = serverAddress.getAddress();
                            port = serverAddress.getPort();
                            logger.info("Selected {}:{}", serverAddress.getAddress(), serverAddress.getPort());
                            break;
                        } else {
                            logger.info("Not in the list");
                        }
                    } catch (NumberFormatException ignored) {
                        logger.info("Not a number or refresh/none");
                    }
                }

                switch (answer) {
                    case "none":
                        return null;
                    case "refresh":
                        return check(multicastClient, scanner, 7);
                    default:
                        logger.info("Unknown argument");
                        break;
                }
            }
        }

        return new ImmutablePair<>(host, port);
    }

    public static void sendMessage(String message) {
        try {
            message = message.replaceAll(" {2}", " ");
            if (!message.equals("") && !message.equals(" ")) {

                if (message.startsWith("/")) {
                    client.sendObject(new CommandPacket(message.substring(1)));
                } else
                    client.sendObject(new MessagePacket(message));
            }
        } catch (IllegalArgumentException e) {
            logger.error("Unable to send the message. Cause: {} {{}}", e.getMessage(), e.getClass().getName());
        }
    }

}
