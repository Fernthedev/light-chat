package com.github.fernthedev.gui;

import java.util.ArrayList;
import java.util.List;

public class PinData {
    private int id;
    private List<FrameData> frameDatas = new ArrayList<>();

    public PinData(int id) {
        this.id = id;
    }

    public List<FrameData> getFrames() {
        return frameDatas;
    }

    public int getId() {
        return id;
    }


    public static class FrameData {
        private int frame;
        private Pinmode pinmode;

        public FrameData(int frame, Pinmode pinm) {
            this.frame = frame;
            this.pinmode = pinm;

        }

        public Pinmode GetPinmode()
        {
            return pinmode;
        }

        public int getFrame()
        {
            return frame;
        }

        public void setPinMode(Pinmode pinmode)
        {
            this.pinmode = pinmode;
        }
    }


    public enum Pinmode
    {
        ON,
        OFF
    }
}
