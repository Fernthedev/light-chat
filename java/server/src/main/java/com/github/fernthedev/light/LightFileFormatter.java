package com.github.fernthedev.light;

import com.github.fernthedev.light.api.LightFile;
import com.github.fernthedev.light.api.LightParser;
import com.github.fernthedev.light.api.lines.LightPinLine;
import com.github.fernthedev.light.api.lines.LightPrintLine;
import com.github.fernthedev.light.api.lines.LightSleepLine;
import com.github.fernthedev.light.api.lines.LightLine;
import com.github.fernthedev.server.Server;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.exception.GpioPinExistsException;
import com.pi4j.system.SystemInfo;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class LightFileFormatter {

    private LightManager lightManager;






    private GpioController gpio;



    public LightFileFormatter(LightManager lightManager, GpioController gpio) {
        this.lightManager = lightManager;
        this.gpio = gpio;




    }

    public void readFormatFile(File file) {
        Thread thread = new Thread(() -> {
            GpioPinDigitalOutput output;

            LightFile lightFile = LightParser.parseFile(file);

            if(lightFile == null) {
                Server.getLogger().info("That is a direcotry, use readfolder");
                return;
            }

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
                        for (GpioPinData pin : lightManager.getPinDataMap().values()) {

                            output = pin.getOutput();


                            if (pinLine.isToggle()) {
                                output.high();
                            } else {
                                output.low();
                            }
                        }
                    }else{
                        output = lightManager.getDataFromInt(pinLine.getPin()).getOutput();

                        Server.getLogger().info(lightManager.getDataFromInt(pinLine.getPin()).getPin().getName() + ":" + pinLine.isToggle());
                        Server.getLogger().info(output.getPin().getName() + " is the pin");

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





}
