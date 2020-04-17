package com.github.fernthedev.lightchat.server.settings;

import com.github.fernthedev.config.gson.GsonConfig;
import com.github.fernthedev.lightchat.core.StaticHandler;
import com.github.fernthedev.lightchat.server.Server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Replaced by {@link GsonConfig}
 */
@Deprecated
public class SettingsManager {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean canClientAccess = true;

    private ServerSettings serverSettings;
    private File settingsFile;

    static {
        new File(getCurrentPath(), "serverSettings.json");
    }

    public static String getCurrentPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    public SettingsManager(File file) {
        settingsFile = file;
    }

    public void setup() {
        serverSettings = new ServerSettings();

        if(!getSettingsFile().exists()) {
            saveSettings();
        }

        load();
    }



    public void load() {
        if(serverSettings == null) {
            serverSettings = new ServerSettings();
            saveSettings();
        }

        if (settingsFile.exists()) {
            try {
                    serverSettings = gson.fromJson(new FileReader(settingsFile), ServerSettings.class);

                    saveSettings();


            } catch (Exception e) {
                if (StaticHandler.isDebug()) {
                    Server.getLogger().error(e.getMessage(), e.getCause());
                }
                settingsFile.delete();


                if (!settingsFile.exists()) {
                    serverSettings = null;
                    load();
                }
            }
        } else {
            Server.getLogger().error("Unable to load serverSettings, seems it is missing");
        }
    }

    public void saveSettings() {
        if(!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                Server.getLogger().error(e.getMessage(), e.getCause());
            }
        }

        try (FileWriter writer = new FileWriter(settingsFile,false)) {
            writer.write(gson.toJson(serverSettings));
        } catch (Exception e) {
            Server.getLogger().error(e.getMessage(), e.getCause());
        }
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public File getSettingsFile() {
        return settingsFile;
    }
}
