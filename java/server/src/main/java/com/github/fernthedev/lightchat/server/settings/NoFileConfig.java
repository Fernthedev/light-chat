package com.github.fernthedev.lightchat.server.settings;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.config.common.exceptions.ConfigNullException;
import lombok.NonNull;

import java.io.File;
import java.util.List;

public class NoFileConfig<T> extends Config<T> {
    public NoFileConfig(@NonNull T configData) {
        super(configData, new File("."));
    }

    /**
     * Saves the file without verifying the contents of the file
     */
    @Override
    public void quickSave() { }

    /**
     * Loads the file
     *
     * @throws ConfigNullException Thrown when the config information is null or malformed.
     */
    @Override
    public void load() {

    }

    /**
     * Should return a String representation of the file {@link #configData}. This string representation should be the way that it is read in {@link #parseConfigFromData(List)}
     *
     * @return String representation of {@link #configData} that is read by {@link #parseConfigFromData(List)}
     */
    @Override
    protected String configToFileString() {
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
