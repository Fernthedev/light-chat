package com.github.fernthedev.lightchat.server.security;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.github.fernthedev.config.gson.GsonConfig;
import com.github.fernthedev.lightchat.server.ClientConnection;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.event.BanEvent;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BanManager {

    private static final File bansFile = new File(getCurrentPath(),"banned.json");

    private Config<? extends BannedData> bannedDataConfig;
    private final Server server;

    public BanManager(Server server) {
        this.server = server;
        try {
            initConfig();
        } catch (ConfigLoadException e) {
            e.printStackTrace();
        }
    }

    protected void initConfig() throws ConfigLoadException {
        bannedDataConfig = new GsonConfig<>(new BannedData(), bansFile);

        bannedDataConfig.load();
    }

    public boolean isBanned(@NonNull ClientConnection clientConnection) {
        return isBanned(clientConnection.getAddress());
    }

    public boolean isBanned(String ip) {
        for(String bannedData : bannedDataConfig.getConfigData().getIpAddresses()) {
            if(bannedData.equalsIgnoreCase(ip)) {
                return true;
            }
        }
        return false;
    }

    public void ban(String ip) {
        BanEvent banEvent = new BanEvent(true, ip);

        server.getPluginManager().callEvent(banEvent);

        if (banEvent.isCancelled()) return;


        bannedDataConfig.getConfigData().getIpAddresses().add(banEvent.getBannedIP());

        try {
            bannedDataConfig.syncSave();
        } catch (ConfigLoadException e) {
            e.printStackTrace();
        }

        closeAllIPs(ip);
    }

    public void unban(String ip) {
        BanEvent banEvent = new BanEvent(false, ip);

        server.getPluginManager().callEvent(banEvent);

        if (banEvent.isCancelled()) return;


        bannedDataConfig.getConfigData().getIpAddresses().remove(banEvent.getBannedIP());
        try {
            bannedDataConfig.syncSave();
        } catch (ConfigLoadException e) {
            e.printStackTrace();
        }
    }


    protected void closeAllIPs(String ip) {
        server.getPlayerHandler().getChannelMap().forEach((channel, clientConnection) -> {
            if (clientConnection.getAddress().equalsIgnoreCase(ip)) clientConnection.close();
        });
    }



    private static File getCurrentPath() {
        return SystemUtils.getUserDir();
    }

    @Getter
    public static class BannedData {
        private List<String> ipAddresses = new ArrayList<>();
    }
}
