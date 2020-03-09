package com.github.fernthedev.terminal.server.backend;

import com.github.fernthedev.server.ClientConnection;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.core.StaticHandler;
import com.google.gson.Gson;
import lombok.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BanManager {

    private static final File bansFile = new File(getCurrentPath(),"banned.json");

    private List<BannedData> banned = new ArrayList<>();

    public boolean isBanned(@NonNull ClientConnection clientConnection) {
        Server.getLogger().info("Checking ban for " + clientConnection);
        if(banned.isEmpty()) load();

        for(BannedData bannedData : banned) {
            Server.getLogger().info("Checking " + clientConnection.getAddress() + " data "+ bannedData);
            if(bannedData.getIp().equals(clientConnection.getAddress())) {
                Server.getLogger().info("Found banned " + bannedData.getIp());
                return true;
            }
        }
        return false;
    }

    public boolean isBanned(String ip) {
        for(BannedData bannedData : banned) {
            if(bannedData.getIp().equals(ip)) {
                return true;
            }
        }
        return false;
    }

    public BanManager() {
        load();
    }

    public void addBan(ClientConnection clientConnection, BannedData bannedData) {
        try(FileWriter writer = new FileWriter(bansFile)) {

            banned.add(bannedData);

            BannedData[] bannedList = banned.toArray(new BannedData[0]);

            writer.write(new Gson().toJson(bannedList));
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientConnection.close();
        load();
    }



    public void load() {
        if(!bansFile.exists()) {
            try {
                bansFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        banned.clear();
        BannedData[] bannedDatas = new Gson().fromJson(StaticHandler.getFile(bansFile),BannedData[].class);
        if(bannedDatas != null) {
            banned.addAll(Arrays.asList(bannedDatas));
        }
    }




    private static String getCurrentPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

}
