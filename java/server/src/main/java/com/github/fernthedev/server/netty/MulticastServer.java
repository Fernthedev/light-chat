package com.github.fernthedev.server.netty;


import com.github.fernthedev.core.MulticastData;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.server.PlayerHandler;
import com.github.fernthedev.server.Server;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class MulticastServer extends QuoteServerThread {

    private Server server;

    private volatile boolean run;

//    @Synchronized
//    private void setRun(boolean run) {
//        this.run = run;
//    }

    public MulticastServer(String name, Server server) throws IOException {
        super(name);
        this.server = server;
        run = true;
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

                MulticastData dataSend = new MulticastData(server.getPort(), StaticHandler.getVersion(), PlayerHandler.players.size());

                buf = new Gson().toJson(dataSend).getBytes();

                InetAddress group = InetAddress.getByName(StaticHandler.address);
                DatagramPacket packet;
                packet = new DatagramPacket(buf, buf.length, group, 4446);
                socket.send(packet);

                try {
                    sleep((long) (Math.random() * TimeUnit.SECONDS.toMillis(5)));
                }
                catch (InterruptedException ignored) { }
            }
            catch (IOException e) {
                e.printStackTrace();
                moreQuotes = false;
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }

}
