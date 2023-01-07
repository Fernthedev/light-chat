package com.github.fernthedev.lightchat.server.settings;

import com.github.fernthedev.config.common.Config;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;

public class NoFileConfig<T> extends Config<T> {
    @SneakyThrows
    public NoFileConfig(@NonNull T configData) {
        super(configData, new File("."));
    }

    /**
     * Saves the file without verifying the contents of the file
     */
    @Override
    public void quickSave() { }

    @Override
    public T load() {
        return configData;
    }

    @Override
    public String configToFileString() {
        return "";
    }


    /**
     * Returns the object instance of {@link #configData} parsed from the file which is saved by {@link #configToFileString()}
     *
     * @param data The String data from the file.
     * @return The object instance.
     */
    @Override
    protected T parseConfigFromData(@NonNull List<String> data) {
        return configData;
    }
}
