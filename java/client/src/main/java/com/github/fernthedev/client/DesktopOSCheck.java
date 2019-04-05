package com.github.fernthedev.client;

public class DesktopOSCheck implements IOSCheck {
    @Override
    public boolean runNatives() {
        return true;
    }
}
