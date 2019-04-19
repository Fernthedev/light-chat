package com.github.fernthedev.server.backend;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Data
public class Settings {

    private String password="password";
    private boolean useMulticast = false;
    private boolean passwordRequiredForLogin = false;
    private boolean useNativeTransport;

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
            default:
                throw new IllegalArgumentException("No such value named " + oldValue + " found");
        }

    }

    public Object getValue(@NotNull String key) {
        switch (key.toLowerCase()) {
            case "password":
                return getPassword();
            case "usemulticast":
                return isUseMulticast();
            case "passwordlogin":
                return isPasswordRequiredForLogin();
            default:
                throw new IllegalArgumentException("No such value named " + key + " found");
        }

    }
}
