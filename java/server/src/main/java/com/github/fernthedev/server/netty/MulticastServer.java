package com.github.fernthedev.server.netty;

@Deprecated
public class MulticastServer  { //extends QuoteServerThread {
/*
    private Server server;

    public MulticastServer(String name,Server server) throws IOException {
        super(name);
        this.server = server;
    }

    public void run() {
        while (moreQuotes) {
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
        }
        socket.close();
    }
*/
}
