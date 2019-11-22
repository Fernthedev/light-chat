package com.github.fernthedev.server.command;

import com.github.fernthedev.light.LightFileFormatter;
import com.github.fernthedev.light.api.LightFile;
import com.github.fernthedev.light.api.LightParser;
import com.github.fernthedev.server.ClientPlayer;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.backend.AuthenticationManager;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;

public class LightCommand extends Command{
    public LightCommand() {
        super("light");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if(args.length > 0) {
            boolean authenticated = false;

            if(sender instanceof ClientPlayer) {
                authenticated = AuthenticationManager.authenticate(sender);
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

                            LightFile lightFile = LightParser.parseFile(file);

                            sender.sendMessage("The file is " + lightFile.toString());
                            sender.sendMessage("Contents" + new GsonBuilder().setPrettyPrinting().create().toJson(lightFile));

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
}
