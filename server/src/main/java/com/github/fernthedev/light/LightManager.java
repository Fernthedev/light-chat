package com.github.fernthedev.light;

import com.github.fernthedev.server.*;
import com.github.fernthedev.server.event.chat.ServerPlugin;
import com.github.fernthedev.universal.StaticHandler;
import com.google.gson.Gson;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;
import com.sun.jna.Platform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class LightManager {

    private GpioPinDigitalOutput output;

    private GpioController gpio;
    private RunLight runLight;

    public static File settingsFile = new File(getCurrentPath(),"settings.json");

    private Settings settings;


    private static String getCurrentPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    public LightManager(Server server) {

        settings = new Settings();



        load();

        boolean runPi4j = false;

        if(settings.useNativeDLLs() ) {

            if(Platform.isLinux()) {
                try {
                    //NativeLibrary.addSearchPath("blinkso", "C:\\blinkso");
                   // String filename = Main.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
                   // System.load("/home/pi/Desktop/libblink.so");

                    LightDLL lightDLL = LightDLL.getINSTANCE();
                    runLight = lightDLL::setLightSwitch;
                } catch (UnsatisfiedLinkError e) {
                    e.printStackTrace();
                    Server.getLogger().error("Unable to find DLL it seems");
                    Server.getLogger().error(e.getMessage(), e.getCause());
                    runPi4j = true;
                }
            }else{
                Server.getLogger().error("Not linux, won't load properly DLLs");;
            }
        }else runPi4j = true;

        if(runPi4j) {
            try {
// create gpio controller

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
        ChangePassword changePassword = new ChangePassword("changepassword",this);
        server.registerCommand(changePassword);
        server.getPluginManager().registerEvents(changePassword, new ServerPlugin());
    }

    private interface RunLight {
        void setLightValue(boolean value);
    }

    private void load() {
        if(!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
                try (FileWriter writer = new FileWriter(settingsFile)) {
                    writer.write(new Gson().toJson(settings));
                } catch (Exception e) {
                    Server.getLogger().error(e.getMessage(), e.getCause());
                }
            } catch (IOException e) {
                Server.getLogger().error(e.getMessage(), e.getCause());
            }
        }

        try {
            settings = new Gson().fromJson(StaticHandler.getFile(settingsFile), Settings.class);
        } catch(Exception e) {
            Server.getLogger().error(e.getMessage(),e.getCause());
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
            writer.write(new Gson().toJson(settings));
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
