package com.github.fernthedev.light;

import com.github.fernthedev.light.api.LightFile;
import com.github.fernthedev.light.api.LightParser;
import com.github.fernthedev.light.api.lines.LightPinLine;
import com.github.fernthedev.light.api.lines.LightPrintLine;
import com.github.fernthedev.light.api.lines.LightSleepLine;
import com.github.fernthedev.light.exceptions.LightFileParseException;
import com.github.fernthedev.light.api.lines.LightLine;
import com.github.fernthedev.server.Server;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.exception.GpioPinExistsException;
import com.pi4j.system.SystemInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LightFileFormatter {

    private LightManager lightManager;

    private Pin[] pins;

    private Map<@NonNull Pin,@NonNull GpioPinData> pinDataMap = new HashMap<>();


    private GpioController gpio;

    {
        try {
            pins = RaspiPin.allPins(SystemInfo.getBoardType());
        } catch (IOException | InterruptedException e) {
            Server.getLogger().error(e.getMessage(),e);
        }

    }

    public LightFileFormatter(LightManager lightManager, GpioController gpio) {
        this.lightManager = lightManager;
        this.gpio = gpio;

        try {
            pins = RaspiPin.allPins(SystemInfo.getBoardType());
        } catch (IOException | InterruptedException e) {
            Server.getLogger().error(e.getMessage(),e);
        }

        try {
            for (Pin pin : pins) {

                pinDataMap.put(pin,new GpioPinData(gpio.provisionDigitalOutputPin(pin, "FileReaderOutput", PinState.HIGH), pin, pin.getAddress()));
            }
        } catch (GpioPinExistsException e) {
            Server.getLogger().info("Unable to check " + e.getMessage());
        }
    }

    public void readFormatFile(File file) {
        Thread thread = new Thread(() -> {
            GpioPinDigitalOutput output;

            LightFile lightFile = LightParser.parseFile(file);

            for(LightLine curLine : lightFile.getLineList()) {
                if (curLine instanceof LightPrintLine) {
                    LightPrintLine pLine = (LightPrintLine) curLine;
                    Server.getLogger().info(pLine.getPrint());
                }

                if (curLine instanceof LightSleepLine) {
                    LightSleepLine sleepLine = (LightSleepLine) curLine;
                    try {
                        Thread.sleep((long) sleepLine.getSleepDouble());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (curLine instanceof LightPinLine) {
                    LightPinLine pinLine = (LightPinLine) curLine;
                    if (pinLine.isAllPins()) {
                        for (GpioPinData pin : pinDataMap.values()) {

                            output = pin.getOutput();


                            if (pinLine.isToggle()) {
                                output.high();
                            } else {
                                output.low();
                            }
                        }
                    }else{
                        output = getDataFromInt(pinLine.getPin()).getOutput();

                        if (pinLine.isToggle()) {
                            output.high();
                        } else {
                            output.low();
                        }
                    }
                }
            }
        },"LightFileReader");

        thread.start();
    }

    /**
     * Read folder directory rather one file
     * @param path The folder directory
     * @throws FileNotFoundException When path is non-existent or not folder
     */
    public void readDirectory(File path) throws FileNotFoundException {
        File[] files = path.listFiles();

        if(!path.exists() || files == null) throw new FileNotFoundException("The folder specified, " + path.getAbsolutePath() + " is either not a folder or does not exist");

        for(File file : files) {

            if(FilenameUtils.getExtension(file.getName()).equals("pia")) {
                readFormatFile(file);
            }

        }
    }

    /**
     * Gets pin from int
     * @param pin The pin int
     * @return The pin instance, null if none found (different raspberry pies have different amount of pins)
     */
    private Pin getPinFromInt(int pin) {
        for(int i =0; i < pins.length;i++) {
            if(pin == i) {
                return pins[i];
            }
        }
        return null;
    }

    private GpioPinData getDataFromInt(int pinInt) {
        Pin pin = getPinFromInt(pinInt);

        if(pinDataMap.get(pin) == null) {
            pinDataMap.put(pin,new GpioPinData(gpio.provisionDigitalOutputPin(pin, "FileReaderOutput", PinState.HIGH), pin, pin.getAddress()));
        }

       // Server.getLogger().info("CHecked " + pinDataMap.get(pin));
      //  Server.getLogger().info("List: " + pinDataMap.keySet().toString());

        return pinDataMap.get(pin);
    }

    @Data
    @AllArgsConstructor
    private class GpioPinData {
        @NonNull
        private GpioPinDigitalOutput output;
        private Pin pin;

        private int pinInt;
    }
}
