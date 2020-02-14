package com.github.fernthedev.terminal.core;

import com.github.fernthedev.core.PacketRegistry;
import com.github.fernthedev.core.StaticHandler;
import com.github.fernthedev.terminal.core.packets.MessagePacket;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonUtil {

    public static void registerTerminalPackets() {
        PacketRegistry.registerPacketPackageFromPacket(new MessagePacket(""));
    }


    public static void startSelfInCmd(String[] args) {
        if (System.console() == null && !StaticHandler.isDebug()) {

            String filename = null;

            try {
                filename = new File(CommonUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.err.println("No console found. Starting with CMD assuming it's Windows");

            String[] newArgs = new String[]{"cmd", "/c", "start", "cmd", "/c", "java -jar \"" + filename + "\" -Xmx2G -Xms2G"};

            List<String> launchArgs = new ArrayList<>(Arrays.asList(newArgs));
            launchArgs.addAll(Arrays.asList(args));

            try {
                Runtime.getRuntime().exec(launchArgs.toArray(new String[]{}));
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
