package com.github.fernthedev.light;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.File;

public interface LightDLL extends Library {


    static LightDLL getINSTANCE()  {
        String currentDirectory;
        File file = new File(".");
        currentDirectory = file.getAbsolutePath();
        return Native.load(currentDirectory + "/libblink.so",
                LightDLL.class);
    }

    void setLightSwitch(boolean value);

}
