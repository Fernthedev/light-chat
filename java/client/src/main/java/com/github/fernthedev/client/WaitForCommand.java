package com.github.fernthedev.client;



@Deprecated
public class WaitForCommand implements Runnable {

    static boolean running;

    protected Client client;
    protected boolean checked;

    public WaitForCommand(Client client) {
        running = false;
        this.client = client;
        checked = false;
    }


    public void run() {
        running = true;

        // client.getLogger().info("Starting the runnable for wait for command ;) " + client.running );
//        while (client.isRunning()) {
//            //  if (client.registered) {
//
//            try {
//                String message = StaticHandler.readLine("> ");
//
//                if (message.equals("")) continue;
//
//                client.sendMessage(message);
//            } catch (UserInterruptException e) {
//                client.close();
//                System.exit(0);
//            }

        //   }
    }
}
