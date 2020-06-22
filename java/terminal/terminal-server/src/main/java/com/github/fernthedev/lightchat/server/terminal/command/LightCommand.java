package com.github.fernthedev.lightchat.server.terminal.command;

import com.github.fernthedev.light.LightFileFormatter;
import com.github.fernthedev.lightchat.server.ClientConnection;
import com.github.fernthedev.lightchat.server.Console;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LightCommand extends Command implements TabExecutor {
    private final Server server;

    public LightCommand(Server server) {
        super("light");
        this.server = server;
    }

    @Override
    public void onCommand(SenderInterface sender, String[] args) {

        if(args.length > 0) {
            boolean authenticated = false;

            if(sender instanceof ClientConnection) {
                authenticated = ServerTerminal.getAuthenticationManager().authenticate(sender);
            }

            if(sender instanceof Console) authenticated = true;

            if (authenticated) {
                String arg = args[0];

                switch (arg.toLowerCase()) {
                    case "readfolder":
                        if(args.length > 1) {
                            String path = args[1];

                            File file = new File(path);

                            if(path.startsWith("./")) {
                                file = new File(System.getProperty("user.dir"),path);
                                path = path.substring(path.indexOf("./") + 1);
                                ServerTerminal.sendMessage(sender, "Reading folder " + path);
                            }

                            try {
                                LightFileFormatter.readDirectory(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                        }else ServerTerminal.sendMessage(sender, "Please specify a file to read.");
                        break;

                    case "readfile":
                        if(args.length > 1) {
                            String path = args[1];

                            File file = new File(path);

                            if(path.startsWith("./")) {
                                file = new File(System.getProperty("user.dir"),path);
                                path = path.substring(path.indexOf("./") + 1);
                                ServerTerminal.sendMessage(sender, "Reading file " + path);
                            }

                            LightFileFormatter.executeLightFile(file);

                        }
                        break;
                    default:
                        ServerTerminal.sendMessage(sender, "No argument found");
                }
            }else{
                ServerTerminal.sendMessage(sender, "Unable to authorize command.");
            }
        }else{
            ServerTerminal.sendMessage(sender, "Incorrect usage. Arguments: off, on, readfile, readfolder");
        }
    }

    @Override
    public List<String> getCompletions(SenderInterface senderInterface, String[] args) {
        if(args.length == 0) {
            return Arrays.asList("off",
                    "on",
                    "readfile",
                    "readfolder");
        }
        return new ArrayList<>();
    }
}
