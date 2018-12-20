package com.github.fernthedev.light;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class Settings {

    private String password="password";
    private boolean useNatives = false;

    public boolean useNatives() {
        return useNatives;
    }

    public void useNatives(boolean useNativeDLLs) {
        this.useNatives = useNativeDLLs;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNewValue(@NotNull String oldValue,@NotNull String newValue) {

        Object value = newValue;

        switch (newValue.toLowerCase()) {
            case "true":
                value = true;
                break;
            case "false":
                value = false;
                break;
        }

        if(StringUtils.isNumeric( newValue)) {
            try {
                value = Integer.parseInt(newValue);
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Incorrect integer value");
            }
        }

        if(newValue.equals("") || oldValue.equals("")) {
            throw new IllegalArgumentException("Values cannot be empty");
        }


        switch (oldValue.toLowerCase()) {
            case "password":
                setPassword((String) value);
                break;
            case "usenatives":
                useNatives((Boolean) value);
                break;
            default:
                throw new IllegalArgumentException("No such value named " + oldValue + " found");
        }

    }

    public Object getValue(@NotNull String key) {
        switch (key.toLowerCase()) {
            case "password":
                return getPassword();
            case "usenatives":
                return useNatives();
            default:
                throw new IllegalArgumentException("No such value named " + key + " found");
        }

    }
}
