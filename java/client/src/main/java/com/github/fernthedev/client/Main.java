package com.github.fernthedev.client;

import com.github.fernthedev.client.netty.MulticastClient;
import com.github.fernthedev.universal.StaticHandler;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    static Scanner scanner;

    static Client client;

    private MulticastClient multicastClient;

    private String host = null;
    private int port = -1;

    private Main(String[] args) {
        AnsiConsole.systemInstall();
        new StaticHandler();
        Logger.getLogger("io.netty").setLevel(Level.OFF);
        StaticHandler.setupLoggers();

        scanner = new Scanner(System.in);


        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equalsIgnoreCase("-port")) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    port = -1;
                }
            }

            if (arg.equalsIgnoreCase("-ip")) {
                try {
                    host = args[i + 1];
                } catch (IndexOutOfBoundsException e) {
                    host = null;
                }
            }

            if (arg.equalsIgnoreCase("-debug")) {
                StaticHandler.isDebug = true;
            }
        }

        if (host == null || host.equals("") || port == -1) {
            multicastClient = new MulticastClient();
            check(4);
        }


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

        while (host == null || host.equalsIgnoreCase("") || port == -1) {
            if (host == null || host.equals(""))
                host = readLine("Host:");

            if (port == -1)
                port = readInt("Port:");
        }


        client = new Client(host, port);

        client.initialize();
    }


    private void check(int amount) {
        multicastClient.checkServers(amount);

        if (!multicastClient.serversAddress.isEmpty()) {
            Map<Integer, ServerAddress> servers = new HashMap<>();
            System.out.println("Select one of these servers, or use none to skip, refresh to refresh");
            int index = 0;
            for (ServerAddress serverAddress : multicastClient.serversAddress) {
                index++;
                servers.put(index, serverAddress);
                if (serverAddress.getVersion().equals(StaticHandler.getVersion())) {
                    System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort());
                } else {
                    System.out.println(">" + index + " | " + serverAddress.getAddress() + ":" + serverAddress.getPort() + " (Server's version is " + serverAddress.getVersion() + " while yours is " + StaticHandler.getVersion() + ")");
                    System.out.println("This server might not work correctly.");
                }
            }

            while (scanner.hasNextLine()) {
                String answer = scanner.nextLine();

                boolean checked = false;

                answer = answer.replaceAll(" ", "");

                if (answer.equalsIgnoreCase("none")) {
                    break;
                }

                if (answer.equalsIgnoreCase("refresh")) {
                    checked = true;
                    check(7);
                }

                if (answer.matches("[0-9]+")) {
                    checked = true;
                    try {
                        int serverIndex = Integer.parseInt(answer);

                        if (servers.containsKey(serverIndex)) {
                            ServerAddress serverAddress = servers.get(index);

                            host = serverAddress.getAddress();
                            port = serverAddress.getPort();
                            System.out.println("Selected " + serverAddress.getAddress() + ":" + serverAddress.getPort());
                            break;
                        } else {
                            System.out.println("Not in the list");
                        }
                    } catch (NumberFormatException ignored) {
                        System.out.println("Not a number or refresh/none");
                    }
                }

                if (!checked) {
                    System.out.println("Unknown argument");
                }
            }
        }
    }

    public static void main(String[] args) {
        new Main(args);
    }

    private static String readLine(String message) {
        //return StaticHandler.readLine(message);

        if (!(message == null || message.equals(""))) {
            System.out.println(message);
        }
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        } else return null;
    }

    private static int readInt(String message) {
        /*String v = StaticHandler.readLine(message);

        if(v.matches("[0-9]+")) {
            return Integer.parseInt(v);
        }else{
            System.err.println("Not a number");
            return readInt(message);
        }*/

        if (!(message == null || message.equals(""))) {
            System.out.println(message);
        }
        if (scanner.hasNextLine()) {
            return scanner.nextInt();
        } else return -1;
    }

}
