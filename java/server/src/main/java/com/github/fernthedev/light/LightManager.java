package com.github.fernthedev.light;

import com.github.fernthedev.server.*;
import com.github.fernthedev.server.event.chat.ServerPlugin;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;

public class LightManager implements Runnable{

    private final Server server;
    private GpioPinDigitalOutput output;

    private GpioController gpio;
    private RunLight runLight;



    private Settings settings;
    private SettingsManager settingsManager;


    public LightManager(Server server,SettingsManager settingsManager) {
        this.server = server;
        this.settingsManager = settingsManager;
    }

    @Override
    public void run() {
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
                    boolean authenticated = false;

                    if(sender instanceof ClientPlayer) {
                        authenticated = ChangePassword.authenticate(sender);
                    }

                    if(sender instanceof Console) authenticated = true;

                    if (authenticated) {
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
                    }else{
                        sender.sendMessage("Unable to authorize command.");
                    }
                }else{
                    sender.sendMessage("Incorrect usage. Arguments: off,on");
                }
            }
        });


    }

    private interface RunLight {
        void setLightValue(boolean value);
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
