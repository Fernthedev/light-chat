package com.github.fernthedev.light;

import com.github.fernthedev.server.Server;
import com.github.fernthedev.universal.StaticHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

@Deprecated
/**
 * Replaced by {@link GsonConfig<}
 */
public class SettingsManager {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean canClientAccess = true;

    private Settings settings;
    private File settingsFile;

    static {
        new File(getCurrentPath(), "settings.json");
    }

    public static String getCurrentPath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    public SettingsManager(File file) {
        settingsFile = file;
    }

    public void setup() {
        settings = new Settings();

        if(!getSettingsFile().exists()) {
            saveSettings();
        }

        load();
    }



    public void load() {
        if(settings == null) {
            settings = new Settings();
            saveSettings();
        }

        if (settingsFile.exists()) {
            try {
                    settings = gson.fromJson(new FileReader(settingsFile), Settings.class);

                    saveSettings();


            } catch (Exception e) {
                if (StaticHandler.isDebug) {
                    Server.getLogger().error(e.getMessage(), e.getCause());
                }
                settingsFile.delete();


                if (!settingsFile.exists()) {
                    settings = null;
                    load();
                }
            }
        } else {
            Server.getLogger().error("Unable to load settings, seems it is missing");
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
            writer.write(gson.toJson(settings));
        } catch (Exception e) {
            Server.getLogger().error(e.getMessage(), e.getCause());
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public File getSettingsFile() {
        return settingsFile;
    }
}
