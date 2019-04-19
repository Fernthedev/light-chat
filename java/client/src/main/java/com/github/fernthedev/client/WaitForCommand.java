package com.github.fernthedev.client;

import com.github.fernthedev.packets.message.MessagePacket;
import com.github.fernthedev.universal.StaticHandler;
import org.jline.reader.UserInterruptException;

public class WaitForCommand implements Runnable {

    static boolean running;

    protected Client client;
    protected boolean checked;

    public WaitForCommand(Client client) {
        running = false;
        this.client = client;
        checked = false;
    }

    public void sendMessage(String message) {
        message = message.replaceAll(" {2}", " ");
        if (!message.equals("") && !message.equals(" ")) {

            /*
            if(client.getClientThread().isAuthenticatePassword()) {
                client.getClientThread().sendObject(new MessagePacket(message,false));
                client.getClientThread().setAuthenticatePassword(false);
                return;
            }*/

            if (message.startsWith("/")) {
                client.getClientThread().sendObject(new MessagePacket(message.substring(1),true));
            } else
                client.getClientThread().sendObject(new MessagePacket(message,false));
        }
    }

    public void run() {
        running = true;

        // client.getLogger().info("Starting the runnable for wait for command ;) " + client.running );
        while (client.running) {
            //  if (client.registered) {

            try {
                String message = StaticHandler.readLine("> ");

                if (message.equals("")) continue;

                sendMessage(message);
            } catch (UserInterruptException e) {
                client.getClientThread().close();
                System.exit(0);
            }

            //   }
        }
    }
}
