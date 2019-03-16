package com.github.fernthedev.light.api.lines;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class LightPinLine extends LightLine{
    private int pin;

    private boolean allPins;

    private boolean toggle;

    public LightPinLine(@NonNull String line, int lineNumber, int pin, boolean toggle) {
        super(line, lineNumber);
        this.pin = pin;
        this.toggle = toggle;
    }

    public LightPinLine(int lineNumber,int pin, boolean toggle) {
        this(formatString(pin,toggle),lineNumber,pin,toggle);
    }

    public LightPinLine(int lineNumber,boolean allPins, boolean toggle) {
        this(formatString(allPins,toggle),lineNumber,allPins,toggle);
    }

    public LightPinLine(LightLine lightLine, int pin,boolean toggle) {
        super(lightLine);
        this.pin = pin;
        this.toggle = toggle;
    }

    public LightPinLine(LightLine lightLine,boolean allPins,boolean toggle) {
        super(lightLine);
        if(!allPins) throw new IllegalArgumentException("If not all pins specify pins");
        handlePins(allPins,toggle);
    }

    public LightPinLine(String line,int lineNumber, boolean allPins,boolean toggle) {
        super(line,lineNumber);
    }

    private void handlePins(boolean allPins,boolean toggle) {
        if(!allPins) throw new IllegalArgumentException("If not all pins specify pins");
        this.allPins = true;
        this.toggle = toggle;
    }

    public static String formatString(int pin,boolean mode) {
        String mod;
        if(mode) mod = "on"; else mod = "off";
        return "pin " + pin + " "  + mod;
    }

    public static String formatString(boolean allPins,boolean mode) {
        String mod;
        if(mode) mod = "on"; else mod = "off";
        return "pin all " + mod;
    }




}
