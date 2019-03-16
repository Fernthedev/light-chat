package com.github.fernthedev.light.api;

import com.github.fernthedev.light.api.lines.LightLine;
import com.github.fernthedev.light.api.lines.LightPinLine;
import com.github.fernthedev.light.api.lines.LightPrintLine;
import com.github.fernthedev.light.api.lines.LightSleepLine;
import com.github.fernthedev.light.exceptions.LightFileParseException;
import com.github.fernthedev.server.Server;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class LightParser {


    public static void saveFile(@NonNull LightFile lightFile,File path) throws IOException {
        File file = new File(lightFile.getFile().getPath());
        if(!file.exists()) {
            file.createNewFile();
        }

        lightFile.setFile(Files.write(lightFile.getFile().toPath(),lightFile.toStringList(), Charset.forName("UTF-8")).toFile());
    }

    public static void saveFolder(@NonNull List<LightFile> files,File path) throws IOException {
        for(LightFile lightFile : files) {
            saveFile(lightFile,path);
        }
    }


    public static LightFile parseFile(@NonNull File file) {
        List<LightLine> lightLines = new ArrayList<>();

        GpioPinDigitalOutput output;
        int lineNumber = 0;

        String concatenateComment = null;
        String[] commented1 = null;
        boolean commented = false;

        LightLine lightLine = null;

        try (Scanner scanner = new Scanner(Objects.requireNonNull(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNumber++;

                String[] checkMessage = line.split(" ", 2);
                List<String> messageWord = new ArrayList<>();


                if (checkMessage.length > 1) {
                    String[] messagewordCheck = line.split(" ");

                    int index = 0;


                    for (String message : messagewordCheck) {
                        if (message == null) continue;

                        message = message.replaceAll(" {2}", " ");

                        index++;
                        if (index == 1 || message.equals("")) continue;


                        messageWord.add(message);
                    }
                }

                line = checkMessage[0];

                boolean found = false;


                line = line.replaceAll(" {2}", " ");
                String[] args = new String[messageWord.size()];
                args = messageWord.toArray(args);

                StringBuilder s = new StringBuilder();

                s.append(line);

                for (String ss : args) {
                    s.append(" ").append(ss);
                }

                lightLine = new LightLine(s.toString(), lineNumber);

                if (line.startsWith(" ")) {
                    line = line.substring(line.indexOf(" ")) + 1;
                }

                if (line.startsWith("//")) {
                    continue;
                }

                if (line.startsWith("#")) {
                    continue;
                }

                if (line.contains("//") || line.contains("#")) {
                    int index;
                    if (line.contains("//")) {
                        index = line.indexOf("//");
                    } else {
                        index = line.indexOf('#');
                    }

                    line = line.substring(index);
                }

                if (line.contains("/*") && !commented) {
                    commented = true;
                    commented1 = line.split("/\\*", 2);

                    concatenateComment = commented1[0];
                }

                if (line.contains("*/") && commented) {
                    String[] commented2 = commented1[1].split("\\*/", 2);

                    concatenateComment += commented2[1];

                    line = concatenateComment;
                    commented = false;
                }

                if (commented) {
                    continue;
                }


                if (line.equalsIgnoreCase("print") && args.length > 1) {
                    StringBuilder st = new StringBuilder();
                    int t = 0;
                    for (String se : args) {
                        if (t > 0) st.append(" ");
                        st.append(se);

                        t++;
                    }

                    lightLines.add(new LightPrintLine(lightLine, st.toString()));
                }

                if (line.equalsIgnoreCase("pin") && args.length > 1) {
                    if (args[0].equalsIgnoreCase("all")) {


                        String newPar = args[1];

                        boolean toggle;

                        if (newPar.equalsIgnoreCase("on")) {
                            toggle = true;
                        } else if (newPar.equalsIgnoreCase("off")) {
                            toggle = false;
                        } else {
                            throw new LightFileParseException(lightLine, "Could not find parameter " + newPar);
                        }

                        lightLines.add(new LightPinLine(lightLine, true, toggle));


                    } else if (args[0].matches("[0-9]+")) {
                        int pinInt = Integer.parseInt(args[0]);


                        String newPar = args[1];
                        boolean toggle;

                        if (newPar.equalsIgnoreCase("on")) {
                            toggle = true;
                        } else if (newPar.equalsIgnoreCase("off")) {
                            toggle = false;
                        } else {
                            throw new LightFileParseException(lightLine, "Could not find parameter " + newPar);
                        }

                        lightLines.add(new LightPinLine(lightLine, pinInt, toggle));


                    } else {
                        throw new LightFileParseException(lightLine, "Argument " + args[0] + " can only be numerical.");
                    }
                }

                if (line.equalsIgnoreCase("sleep")) {
                    if (args.length > 0) {
                        String amount = args[0];
                        if (amount.replaceAll("\\.", "").matches("[0-9]+")) {
                            double time = Double.parseDouble(amount);
                            lightLines.add(new LightSleepLine(lightLine, time));
                        }
                    }
                }

            }
        } catch (FileNotFoundException e) {
            Server.getLogger().error(e.getMessage(), e);
        } catch (Exception e) {
            if (lightLine != null)
                throw new LightFileParseException(lightLine, e);
            else e.printStackTrace();
        }

        return new LightFile(file, lightLines);
    }

    /**
     * Read folder directory rather one file
     * @param path The folder directory
     * @throws FileNotFoundException When path is non-existent or not folder
     */
    public static List<LightFile> parseFolder(@NonNull File path) throws FileNotFoundException {
        File[] files = path.listFiles();

        List<LightFile> lightFiles = new ArrayList<>();

        if(!path.exists() || files == null) throw new FileNotFoundException("The folder specified, " + path.getAbsolutePath() + " is either not a folder or does not exist");

        for(File file : files) {

            if(FilenameUtils.getExtension(file.getName()).equals("pia")) {
                lightFiles.add(parseFile(file));
            }

        }
        return lightFiles;
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
