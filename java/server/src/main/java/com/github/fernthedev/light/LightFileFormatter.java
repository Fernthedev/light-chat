package com.github.fernthedev.light;

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
            int lineNumber = 0;

            String concatenateComment = null;
            String[] commented1 = null;
            boolean commented = false;

            LightLine lightLine = null;

            try (Scanner scanner = new Scanner(Objects.requireNonNull(file))) {
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    lineNumber++;

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

                    StringBuilder s = new StringBuilder();

                    s.append(line);

                    for(String ss : args) {
                        s.append(" ").append(ss);
                    }

                    lightLine = new LightLine(s.toString(),lineNumber);

                    if(line.startsWith(" ") ) {
                        line = line.substring(line.indexOf(" ")) + 1;
                    }

                    if(line.startsWith("//") ) {
                        continue;
                    }

                    if(line.startsWith("#")) {
                        continue;
                    }

                    if(line.contains("//") || line.contains("#")) {
                        int index;
                        if(line.contains("//")) {
                            index = line.indexOf("//");
                        }else{
                            index = line.indexOf('#');
                        }

                        line = line.substring(index);
                    }

                    if(line.contains("/*") && !commented) {
                        commented = true;
                        commented1 = line.split("/\\*", 2);

                        concatenateComment = commented1[0];
                    }

                    if(line.contains("*/") && commented) {
                        String[] commented2 = commented1[1].split("\\*/", 2);

                        concatenateComment += commented2[1];

                        line = concatenateComment;
                        commented = false;
                    }

                    if(commented) {
                        continue;
                    }


                    if(line.equalsIgnoreCase("print") && args.length > 1) {
                        StringBuilder st = new StringBuilder();
                        int t = 0;
                        for(String se : args) {
                            if(t > 0) st.append(" ");
                            st.append(se);

                            t++;
                        }
                        Server.getLogger().info(st.toString());
                    }

                    if(line.equalsIgnoreCase("pin") && args.length > 1) {
                        if(args[0].equalsIgnoreCase("all")) {
                            for(GpioPinData pin : pinDataMap.values()) {
                                Server.getLogger().info("Checking pin " + pin.getPin().getAddress());

                                output = pin.getOutput();

                                String newPar = args[1];
                                if (newPar.equalsIgnoreCase("on")) {
                                   output.high();
                                } else if (newPar.equalsIgnoreCase("off")) {
                                    output.low();
                                } else {
                                    throw new LightFileParseException(lightLine, "Could not find parameter " + newPar);
                                }


                            }
                        }else if(args[0].matches("[0-9]+")) {
                            int pinInt = Integer.parseInt(args[0]);

                            Pin pin = getPinFromInt(pinInt);

                            Server.getLogger().info("Checking pin " + pinInt);

                            if(pin == null) {
                                throw new LightFileParseException(lightLine,"Pin could not be found. The pins found are: " + pins.length + " " + Arrays.toString(pins));
                            }else {
                                if(getDataFromInt(pinInt) == null) {
                                    throw new LightFileParseException(lightLine,"The pin attempted to access has not been registered. Try restarting the server. The registered pin list is: " + pinDataMap.keySet());
                                }

                                output = pinDataMap.get(pin).getOutput();


                                String newPar = args[1];
                                if (newPar.equalsIgnoreCase("on")) {
                                    output.high();
                                } else if (newPar.equalsIgnoreCase("off")) {
                                    output.low();
                                } else {
                                    throw new LightFileParseException(lightLine,"Could not find parameter " + newPar);
                                }


                            }

                        }else{
                            throw new LightFileParseException(lightLine,"Argument " + args[0] + " can only be numerical.");
                        }
                    }

                    if(line.equalsIgnoreCase("sleep")) {
                        if(args.length > 0) {
                            String amount = args[0];
                            if(amount.replaceAll("\\.","").matches("[0-9]+")) {
                                double time = Double.parseDouble(amount);
                                Thread.sleep(TimeUnit.SECONDS.toMillis((long) time));
                            }
                        }
                    }

                }
            } catch (FileNotFoundException | InterruptedException e) {
                Server.getLogger().error(e.getMessage(),e);
            } catch (Exception e) {
                if(lightLine != null)
                throw new LightFileParseException(lightLine,e);
                else e.printStackTrace();
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
