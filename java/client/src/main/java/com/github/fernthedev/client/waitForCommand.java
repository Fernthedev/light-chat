package com.github.fernthedev.client;

import com.github.fernthedev.packets.CommandPacket;
import com.github.fernthedev.packets.MessagePacket;

import java.util.Scanner;

public class waitForCommand implements Runnable {

    static boolean running;

    private Scanner scanner;
    private Client client;
    private boolean checked;

    waitForCommand(Client client) {
        running = false;
        this.client = client;
        this.scanner = Main.scanner;
        checked = false;
    }


    public void run() {
        running = true;
        // client.getLogger().info("Starting the runnable for wait for command ;) " + client.running );
        while (client.running) {
            //  if (client.registered) {

            //if (scanner.hasNextLine()) {
            if (!checked) {
                Client.getLogger().info("Type Command:");
                checked = true;
            }
            String sendmessage = scanner.nextLine();
            sendmessage = sendmessage.replaceAll(" {2}", " ");
            if (!sendmessage.equals("") && !sendmessage.equals(" ")) {

                if (sendmessage.startsWith("/")) {
                    client.getClientThread().sendObject(new CommandPacket(sendmessage.substring(1)));
                } else
                    client.getClientThread().sendObject(new MessagePacket(sendmessage));
            }
            // }
            //   }
        }
    }
}
