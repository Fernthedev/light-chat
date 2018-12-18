package com.github.fernthedev.client.netty;

@Deprecated
public class MulticastClient {
/*
    public List<ServerAddress> serversAddress = new ArrayList<>();

    private Map<String,ServerAddress> addressServerAddressMap = new HashMap<>();

    public void checkServers(int amount) {
        try {
            MulticastSocket socket = new MulticastSocket(4446);
            InetAddress group = InetAddress.getByName(StaticHandler.address);
            socket.joinGroup(group);

            DatagramPacket packet;
            for (int i = 0; i < amount; i++) {

                byte[] buf = new byte[256];
                packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);

                String received = new String(packet.getData());

                if(received.equals("")) {
                    continue;
                }

                received = received.replaceAll(" ","");

                try {
                    JsonReader reader = new JsonReader(new StringReader(received));

                    reader.setLenient(true);

                    MulticastData data = new Gson().fromJson(reader, MulticastData.class);

                    String address = (packet.getAddress()).getHostAddress();

                    if(!addressServerAddressMap.containsKey(address)) {
                        ServerAddress serverAddress = new ServerAddress(address, data.getPort(), data.getVersion());
                        serversAddress.add(serverAddress);
                        addressServerAddressMap.put(address,serverAddress);
                    }
                } catch (Exception e) {
                    if(StaticHandler.isDebug) {
                        throw new DebugChainedException(e,"Unable to read packet");
                    }
                }

            }

            socket.leaveGroup(group);
            socket.close();
        } catch (IOException | DebugChainedException e) {
            e.printStackTrace();
        }
    }*/
}
