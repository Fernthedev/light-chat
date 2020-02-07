package com.github.fernthedev.server.command;

import com.github.fernthedev.light.LightFileFormatter;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.Server;

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
    public void onCommand(CommandSender sender, String[] args) {

        if(args.length > 0) {
            boolean authenticated = false;

            if(sender instanceof ClientPlayer) {
                authenticated = server.getAuthenticationManager().authenticate(sender);
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
                                sender.sendMessage("Reading folder " + path);
                            }

                            try {
                                LightFileFormatter.readDirectory(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                        }else sender.sendMessage("Please specify a file to read.");
                        break;

                    case "readfile":
                        if(args.length > 1) {
                            String path = args[1];

                            File file = new File(path);

                            if(path.startsWith("./")) {
                                file = new File(System.getProperty("user.dir"),path);
                                path = path.substring(path.indexOf("./") + 1);
                                sender.sendMessage("Reading file " + path);
                            }

                            LightFileFormatter.executeLightFile(file);

                        }
                        break;
                    default:
                        sender.sendMessage("No argument found");
                }
            }else{
                sender.sendMessage("Unable to authorize command.");
            }
        }else{
            sender.sendMessage("Incorrect usage. Arguments: off, on, readfile, readfolder");
        }
    }

    @Override
    public List<String> getCompletions(String[] args) {
        if(args.length == 0) {
            return Arrays.asList("off",
                    "on",
                    "readfile",
                    "readfolder");
        }
        return new ArrayList<>();
    }
}
