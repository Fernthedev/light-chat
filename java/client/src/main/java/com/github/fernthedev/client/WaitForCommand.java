package com.github.fernthedev.client;

import com.github.fernthedev.packets.CommandPacket;
import com.github.fernthedev.packets.MessagePacket;
import com.github.fernthedev.universal.StaticHandler;

import java.util.Scanner;

public class WaitForCommand implements Runnable {

    static boolean running;

    protected Scanner scanner;
    protected Client client;
    protected boolean checked;

    public WaitForCommand(Client client) {
        running = false;
        this.client = client;
        checked = false;
    }

    protected void setScanner() {
        this.scanner = Main.scanner;
    }

    public void sendMessage(String message) {
        message = message.replaceAll(" {2}", " ");
        if (!message.equals("") && !message.equals(" ")) {

            if (message.startsWith("/")) {
                client.getClientThread().sendObject(new CommandPacket(message.substring(1)));
            } else
                client.getClientThread().sendObject(new MessagePacket(message));
        }
    }

    public void run() {
        running = true;
        setScanner();
        // client.getLogger().info("Starting the runnable for wait for command ;) " + client.running );
        while (client.running && scanner.hasNextLine()) {
            //  if (client.registered) {

            if (scanner.hasNextLine()) {
                String message = StaticHandler.readLine("> ");
                sendMessage(message);
            }

            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //   }
        }
    }
}
