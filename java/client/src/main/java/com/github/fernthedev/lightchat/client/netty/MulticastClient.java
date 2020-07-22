package com.github.fernthedev.lightchat.client.netty;


import com.github.fernthedev.lightchat.core.MulticastData;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.core.exceptions.DebugChainedException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.Getter;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MulticastClient {

    @Getter
    private final List<MulticastData> serversAddress = new ArrayList<>();

    private Map<String, MulticastData> addressServerAddressMap = new HashMap<>();

    public void checkServers(int amount) {
        try(MulticastSocket socket = new MulticastSocket(4446)) {

            InetAddress group = InetAddress.getByName(StaticHandler.getMulticastAddress());
            socket.setSoTimeout(2000);
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

                parseData(packet, received);

            }

            socket.leaveGroup(group);
        }catch (SocketTimeoutException ignored) {

        } catch (IOException | DebugChainedException e) {
            e.printStackTrace();
        }
    }

    private void parseData(DatagramPacket packet, String received) {
        try (JsonReader reader = new JsonReader(new StringReader(received))) {
            reader.setLenient(true);

            MulticastData data = new Gson().fromJson(reader, MulticastData.class);

            String address = (packet.getAddress()).getHostAddress();
            data.setAddress(address);

            if(!addressServerAddressMap.containsKey(address)) {
                serversAddress.add(data);
                addressServerAddressMap.put(address, data);
            }
        } catch (Exception e) {
            if(StaticHandler.isDebug()) {
                throw new DebugChainedException(e, "Unable to read packet");
            }
        }
    }
}
