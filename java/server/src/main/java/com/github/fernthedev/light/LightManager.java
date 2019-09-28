package com.github.fernthedev.light;

import com.github.fernthedev.server.*;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.universal.ColorCode;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.exception.GpioPinExistsException;
import com.pi4j.system.SystemInfo;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LightManager implements Runnable{

    private final Server server;
    private GpioPinDigitalOutput output;

    private GpioController gpio;

    @Getter
    private Map<@NonNull Pin,@NonNull GpioPinData> pinDataMap = new HashMap<>();

    @Getter
    private Pin[] pins;


    public LightManager(Server server) {
        this.server = server;

        gpio = GpioFactory.getInstance();

        try {
            pins = RaspiPin.allPins(SystemInfo.getBoardType());
        } catch (IOException | InterruptedException e) {
            Server.getLogger().error(e.getMessage(),e);
        }

        try {
            if (pins != null) {
                for (Pin pin : pins) {

                    pinDataMap.put(pin,new GpioPinData(gpio.provisionDigitalOutputPin(pin, "LightManager"+pin.getName(), PinState.HIGH), pin, pin.getAddress()));
                }
            }
        } catch (GpioPinExistsException e) {
            Server.getLogger().info( "{}Unable to check {}", ColorCode.RED, e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
        // create gpio controller
            Server.getLogger().info("Loading pi4j java");




        } catch (UnsatisfiedLinkError | Exception e) {
            Server.getLogger().error(e.getMessage(), e.getCause());
        }

        LightFileFormatter fileFormatter = new LightFileFormatter(this,gpio);

        server.registerCommand(new Command("light") {
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

                        if(arg.matches("[0-9]+")) {
                            int pinInt = Integer.parseInt(arg);

                            if(args.length < 1) {
                                return;
                            }



                        }else switch (arg.toLowerCase()) {
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
                                        fileFormatter.readDirectory(file);
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

                                    fileFormatter.readFormatFile(file);

                                }
                                break;
                            default:
                                sender.sendMessage("No argument found");
                        }
                    }else{
                        sender.sendMessage("Unable to authorize command.");
                    }
                }else{
                    sender.sendMessage("Incorrect usage. Arguments: off,on, readfile, readfolder");
                }
            }
        });


    }

    private interface RunLight {
        void setLightValue(boolean value);
    }

    public GpioPinData getDataFromInt(int pinInt) {

        @NonNull Pin pin = getPinFromInt(pinInt);


        Server.getLogger().info(pin);

        if(getPinDataMap().get(pin) == null) {
            getPinDataMap().put(pin,new GpioPinData(gpio.provisionDigitalOutputPin(pin, "GPIOData"+pinInt, PinState.HIGH), pin, pinInt));
        }


        // Server.getLogger().info("CHecked " + lightManager.getPinDataMap().get(pin));
        //  Server.getLogger().info("List: " + lightManager.getPinDataMap().keySet().toString());

        return getPinDataMap().get(pin);
    }

    /**
     * Gets pin from int
     * @param pin The pin int
     * @return The pin instance, null if none found (different raspberry pies have different amount of pins)
     */
    private Pin getPinFromInt(int pin) {
        return RaspiPin.getPinByAddress(pin);
        /*
        for(int i =0; i < pins.length;i++) {
            if(pin == i) {
                return pins[i];
            }
        }*/

        //return null;
    }

}
