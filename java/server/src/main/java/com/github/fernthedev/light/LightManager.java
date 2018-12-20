package com.github.fernthedev.light;

import com.github.fernthedev.server.Command;
import com.github.fernthedev.server.CommandSender;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.event.chat.ServerPlugin;
import com.github.fernthedev.universal.StaticHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class LightManager implements Runnable{

    private final Server server;
    private GpioPinDigitalOutput output;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private GpioController gpio;
    private RunLight runLight;

    public static File settingsFile = new File(getCurrentPath(),"settings.json");

    private Settings settings;


    private static String getCurrentPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    public LightManager(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        settings = new Settings();

        if(!settingsFile.exists()) {
            saveSettings();
        }

        load();


        try {
        // create gpio controller
            Server.getLogger().info("Loading pi4j java");
            gpio = GpioFactory.getInstance();
            // lookup the pin by address
            Pin pin = CommandArgumentParser.getPin(
                    RaspiPin.class,    // pin provider class to obtain pin instance from
                    RaspiPin.GPIO_06);             // argument array to search in
            // We are using PIN 06 as per the attached diagram
            output = gpio.provisionDigitalOutputPin(pin, "My Output", PinState.HIGH);
            // switch ON
            output.high();
            // switch OFF
            output.low();

            runLight = value -> {
                if(value) {
                    output.high();
                }
                if(!value) {
                    output.low();
                }
            };
        } catch (UnsatisfiedLinkError | Exception e) {
            Server.getLogger().error(e.getMessage(), e.getCause());
        }

        server.registerCommand(new Command("light") {
            @Override
            public void onCommand(CommandSender sender, String[] args) {
                if(args.length > 0) {
                    if (sender instanceof Console) {
                        String arg = args[0];

                        switch (arg.toLowerCase()) {
                            case "off":
                                setOff();
                                sender.sendMessage("Set light to off");
                                break;
                            case "on":
                                setOn();
                                sender.sendMessage("Set light to on");
                                break;
                            default:
                                sender.sendMessage("No argument found");
                        }
                    }else sender.sendMessage("This feature is still being made for clients");
                }else{
                    sender.sendMessage("Incorrect usage. Arguments: off,on");
                }
            }
        });
        server.registerCommand(new Command("settings") {
            @Override
            public void onCommand(CommandSender sender, String[] args) {

                if(args.length == 0) {
                    sender.sendMessage("Possible args: set,get,reload,save");
                }else {
                    boolean authenticated = ChangePassword.authenticate(sender);
                    if (authenticated) {
                        long timeStart, timeEnd, timeElapsed;
                        String arg = args[0];

                        switch (arg.toLowerCase()) {
                            case "set":
                                if (args.length > 2) {
                                    String oldValue = args[1];
                                    String newValue = args[2];

                                    try {
                                        settings.setNewValue(oldValue, newValue);
                                        sender.sendMessage("Set " + oldValue + " to " + newValue);
                                    } catch (ClassCastException | IllegalArgumentException e) {
                                        sender.sendMessage("Error:" + e.getMessage());
                                    }
                                } else sender.sendMessage("Usage: settings set {oldvalue} {newvalue}");
                                break;

                            case "get":
                                if (args.length > 1) {
                                    String key = args[1];

                                    try {
                                        Object value = settings.getValue(key);
                                        sender.sendMessage("Value of " + key + ": " + value);
                                    } catch (ClassCastException | IllegalArgumentException e) {
                                        sender.sendMessage("Error:" + e.getMessage());
                                    }
                                } else sender.sendMessage("Usage: settings get {key}");
                                break;

                            case "reload":
                                sender.sendMessage("Reloading.");
                                timeStart = System.nanoTime();
                                saveSettings();

                                load();
                                timeEnd = System.nanoTime();
                                timeElapsed = (timeEnd - timeStart) / 1000000;
                                sender.sendMessage("Finished reloading. Took " + timeElapsed + "ms");
                                break;

                            case "save":
                                sender.sendMessage("Saving.");
                                timeStart = System.nanoTime();
                                saveSettings();
                                timeEnd = System.nanoTime();
                                timeElapsed = (timeEnd - timeStart) / 1000000;
                                sender.sendMessage("Finished saving. Took " + timeElapsed + "ms");
                                break;
                            default:
                                sender.sendMessage("No such argument found " + arg + " found");
                                break;
                        }
                    }
                }
            }
        });
        ChangePassword changePassword = new ChangePassword("changepassword",this);
        server.registerCommand(changePassword);
        server.getPluginManager().registerEvents(changePassword, new ServerPlugin());
    }

    private interface RunLight {
        void setLightValue(boolean value);
    }

    private void load() {
        if(settings == null) {
            settings = new Settings();
            saveSettings();
        }

        if (settingsFile.exists()) {
            try {
                try(JsonReader reader = new JsonReader(new FileReader(settingsFile))) {

                    settings = gson.fromJson(reader, Settings.class);
                    saveSettings();

                }
            } catch (Exception e) {
                if (StaticHandler.isDebug) {
                    Server.getLogger().error(e.getMessage(), e.getCause());
                }
                settingsFile.delete();
                if (!settingsFile.exists()) {
                    settings = null;
                    load();
                }
            }
        } else {
            Server.getLogger().error("Unable to load settings, seems it is missing");
        }
    }

    public void saveSettings() {
        if(!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                Server.getLogger().error(e.getMessage(), e.getCause());
            }
        }

        try (FileWriter writer = new FileWriter(settingsFile)) {
            writer.write(gson.toJson(settings));
        } catch (Exception e) {
            Server.getLogger().error(e.getMessage(), e.getCause());
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void setOn() {
        runLight.setLightValue(true);
    }

    public void setOff() {
        runLight.setLightValue(false);
    }



}
