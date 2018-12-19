package com.github.fernthedev.light;

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
}
