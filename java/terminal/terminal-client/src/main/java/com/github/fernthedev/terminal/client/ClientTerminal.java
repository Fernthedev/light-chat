package com.github.fernthedev.terminal.client;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.client.netty.MulticastClient;
import com.github.fernthedev.core.MulticastData;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.core.VersionData;
import com.github.fernthedev.terminal.core.CommonUtil;
import com.github.fernthedev.terminal.core.ConsoleHandler;
import com.github.fernthedev.terminal.core.TermCore;
import com.github.fernthedev.terminal.core.packets.CommandPacket;
import com.github.fernthedev.terminal.core.packets.MessagePacket;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ClientTerminal {

    @Getter
    private static Logger logger = LoggerFactory.getLogger(ClientTerminal.class);

    @Getter
    private static AutoCompleteHandler autoCompleteHandler;
    private static Client client;

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        java.util.logging.Logger.getLogger("io.netty").setLevel(Level.OFF);
        StaticHandler.setupLoggers();

        String host = null;
        int port = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equalsIgnoreCase("-port")) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                    if (port < 0) {
                        logger.error("-port cannot be less than 0");
                        port = -1;
                    } else logger.info("Using port {}", args[i+ + 1]);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    logger.error("-port is not a number");
                    port = -1;
                }
            }

            if (arg.equalsIgnoreCase("-ip") || arg.equalsIgnoreCase("-host")) {
                try {
                    host = args[i + 1];
                    logger.info("Using host {}", args[i+ + 1]);
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Cannot find argument for -host");
                    host = null;
                }
            }

            if (arg.equalsIgnoreCase("-debug")) {
                StaticHandler.setDebug(true);
                logger.debug("Debug enabled");
            }
        }




        if (System.console() == null && !StaticHandler.isDebug()) {

            String filename = Client.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            logger.info("No console found. Starting with CMD assuming it's Windows");

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

        MulticastClient multicastClient;
        Scanner scanner = new Scanner(System.in);
        if (host == null || host.equals("") || port == -1) {
            multicastClient = new MulticastClient();
            Pair<String, Integer> hostPortPair = check(multicastClient, scanner,4);

            if (hostPortPair != null) {
                host = hostPortPair.getLeft();
                port = hostPortPair.getRight();
            }
        }

        while (host == null || host.equalsIgnoreCase("") || port == -1) {
            if (host == null || host.equals(""))
                host = readLine(scanner, "Host:");

            if (port == -1)
                port = readInt(scanner, "Port:");
        }




        client = new Client(host, port);

        StaticHandler.setCore(new ClientTermCore(client));
        CommonUtil.registerTerminalPackets();

        autoCompleteHandler = new AutoCompleteHandler(client);

        ConsoleHandler.startConsoleHandlerAsync((TermCore) StaticHandler.getCore(), autoCompleteHandler);

        client.addPacketHandler(new PacketHandler());

        client.connect();

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


    private static Pair<String, Integer> check(MulticastClient multicastClient, Scanner scanner, int amount) {
        logger.info("Looking for MultiCast servers");
        multicastClient.checkServers(amount);

        String host = null;
        int port = -1;

        if (!multicastClient.serversAddress.isEmpty()) {
            Map<Integer, MulticastData> servers = new HashMap<>();
            logger.info("Select one of these servers, or use none to skip, refresh to refresh");
            int index = 0;
            for (MulticastData serverAddress : multicastClient.serversAddress) {
                index++;
                servers.put(index, serverAddress);

                DefaultArtifactVersion serverCurrent = new DefaultArtifactVersion(serverAddress.getVersion());
                DefaultArtifactVersion serverMin = new DefaultArtifactVersion(serverAddress.getMinVersion());

                StaticHandler.VERSION_RANGE range = StaticHandler.getVersionRangeStatus(new VersionData(serverCurrent, serverMin));

                if (range == StaticHandler.VERSION_RANGE.MATCH_REQUIREMENTS){
                    System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort());
                } else {
                    // Current version is smaller than the server's required minimum
                    if(range == StaticHandler.VERSION_RANGE.WE_ARE_LOWER) {
                        System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort() + " (Server's required minimum version is " + serverAddress.getMinVersion() + " while your current version is smaller {" + StaticHandler.getVERSION_DATA().getVersion() + "} Incompatibility issues may arise)");
                    }

                    // Current version is larger than server's minimum version
                    if (range == StaticHandler.VERSION_RANGE.WE_ARE_HIGHER) {
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
            logger.error("Unable to send message. Cause: " + e.getMessage() + " {" + e.getClass().getName() + "}");
        }
    }

}
