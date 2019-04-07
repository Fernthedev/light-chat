package com.github.fernthedev.light.api.lines;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class LightSleepLine extends LightLine {

    private double sleepDouble;

    public LightSleepLine(@NonNull String line, int lineNumber, double sleepDouble) {
        super(line, lineNumber);
        this.sleepDouble = sleepDouble;
    }

    public LightSleepLine(LightLine lightLine, double sleepDouble) {
        super(lightLine);
        this.sleepDouble = sleepDouble;
    }

    public static String formatString(double sleep) {
        return "sleep " + sleep;
    }

}
