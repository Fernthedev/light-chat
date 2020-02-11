package com.github.fernthedev.server.netty;


import com.github.fernthedev.core.MulticastData;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.server.PlayerHandler;
import com.github.fernthedev.server.Server;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class MulticastServer extends QuoteServerThread {

    private final String multicastAddress;
    private Server server;

    private volatile boolean run;



//    @Synchronized
//    private void setRun(boolean run) {
//        this.run = run;
//    }

    public MulticastServer(String name, Server server, String multicastAddress) throws IOException {
        super(name);
        this.server = server;
        run = true;
        this.multicastAddress = multicastAddress;
//        setRun(true);
    }

    public void stopMulticast() {
        run = false;
//        setRun(false);
    }

    public void run() {
        while (moreQuotes && run) {
            try {
                byte[] buf;
                // don't wait for request...just send a quote

                MulticastData dataSend = new MulticastData(server.getPort(), StaticHandler.getVERSION_DATA().getVariablesJSON().getVersion(), StaticHandler.getVERSION_DATA().getVariablesJSON().getMinVersion(), PlayerHandler.players.size());

                buf = new Gson().toJson(dataSend).getBytes();

                InetAddress group = InetAddress.getByName(multicastAddress);
                DatagramPacket packet;
                packet = new DatagramPacket(buf, buf.length, group, 4446);
                socket.send(packet);

                try {
                    sleep((long) (Math.random() * 200));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                moreQuotes = false;
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Server.getLogger().info("Closing MultiCast Server");
        socket.close();
    }

}
