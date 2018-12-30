package com.github.fernthedev.light;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface LightDLL extends Library {


    static LightDLL getINSTANCE()  {
        String currentDirectory = System.getProperty("user.dir");
        return Native.load("blink", LightDLL.class);
    }

    void setLightSwitch(int pinSwitch, boolean var);
    void setLightSwitch(boolean var);

}
