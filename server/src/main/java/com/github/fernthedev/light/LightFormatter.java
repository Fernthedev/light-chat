package com.github.fernthedev.light;

import com.github.fernthedev.server.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class LightFormatter {

    private LightManager lightManager;

    public LightFormatter(LightManager lightManager) {
        this.lightManager = lightManager;
    }

    public void readFormatFile(File file) {

        Thread thread = new Thread(() -> {
            try (Scanner scanner = new Scanner(Objects.requireNonNull(file))) {
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] checkMessage = line.split(" ", 2);
                    List<String> messageWord = new ArrayList<>();

                    if (checkMessage.length > 1) {
                        int index = 0;
                        for(String message : checkMessage) {
                            index++;
                            if(index == 1 || message == null || message.equals("") || message.equals(" ")) {
                                continue;
                            }


                            messageWord.add(message);
                        }
                    }

                    if(line.equalsIgnoreCase("on")) {
                        lightManager.setOn();
                    }

                    if(line.equalsIgnoreCase("off")) {
                        lightManager.setOff();
                    }

                    if(line.contains("sleep ")) {
                        if(checkMessage.length > 1) {
                            String amount = checkMessage[1];
                            if(amount.matches("[0-9]+")) {
                                long time = Long.parseLong(amount);
                                Thread.sleep(time);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException | InterruptedException e) {
                Server.getLogger().error(e.getMessage(),e.getCause());
            }
        });



    }

}
