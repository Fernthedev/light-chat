package com.github.fernthedev.light;

import com.github.fernthedev.server.Server;
import com.pi4j.io.gpio.*;
import com.pi4j.system.SystemInfo;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class LightFileFormatter {

    private LightManager lightManager;

    private Pin[] pins;

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
    }

    public void readFormatFile(File file) {

        Thread thread = new Thread(() -> {
            GpioPinDigitalOutput output;
            try (Scanner scanner = new Scanner(Objects.requireNonNull(file))) {
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    String[] checkMessage = line.split(" ", 2);
                    List<String> messageWord = new ArrayList<>();


                    if (checkMessage.length > 1) {
                        String [] messagewordCheck = line.split(" ");

                        int index = 0;



                        for(String message : messagewordCheck) {
                            if(message == null) continue;

                            message = message.replaceAll(" {2}"," ");

                            index++;
                            if(index == 1 || message.equals("")) continue;


                            messageWord.add(message);
                        }
                    }

                    line = checkMessage[0];

                    boolean found = false;



                    line = line.replaceAll(" {2}"," ");
                    String[] args = new String[messageWord.size()];
                    args = messageWord.toArray(args);

                    if(line.equalsIgnoreCase("pin") && args.length > 1) {
                        if(args[0].matches("[0-9]+")) {
                            int pinInt = Integer.parseInt(args[0]);

                            Pin pin = getPinFromInt(pinInt);

                            if(pin == null) {
                                throw new IllegalArgumentException("Pin could not be found. The pins found are: " + pins.length + " " + Arrays.toString(pins));
                            }else {
                                output = gpio.provisionDigitalOutputPin(pin, "FileReaderOutput", PinState.HIGH);

                                String newPar = args[1];
                                if (newPar.equalsIgnoreCase("on")) {
                                    output.high();
                                } else if (newPar.equalsIgnoreCase("off")) {
                                    output.low();
                                } else {
                                    throw new IllegalArgumentException("Could not find parameter " + newPar);
                                }
                            }

                        }else{
                            throw new IllegalArgumentException("Argument " + args[0] + " can only be numerical.");
                        }
                    }

                    if(line.equalsIgnoreCase("sleep")) {
                        if(args.length > 0) {
                            String amount = args[0];
                            if(amount.replaceAll("\\.","").matches("[0-9]+")) {
                                double time = Double.parseDouble(amount);
                                Thread.sleep((long) time);
                            }
                        }
                    }

                }
            } catch (FileNotFoundException | InterruptedException e) {
                Server.getLogger().error(e.getMessage(),e.getCause());
            }
        });

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

}
