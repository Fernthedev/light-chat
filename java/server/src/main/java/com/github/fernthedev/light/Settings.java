package com.github.fernthedev.light;

public class Settings {

    private String password="password";
    private boolean useNativeDLLs = true;

    public boolean useNativeDLLs() {
        return useNativeDLLs;
    }

    public void useUseNativeDLLs(boolean useNativeDLLs) {
        this.useNativeDLLs = useNativeDLLs;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
