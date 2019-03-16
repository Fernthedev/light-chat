package com.github.fernthedev.light.api.lines;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class LightSleepLine extends LightLine {

    private double sleepInt;

    public LightSleepLine(@NonNull String line, int lineNumber, double sleepInt) {
        super(line, lineNumber);
        this.sleepInt = sleepInt;
    }

    public LightSleepLine(LightLine lightLine, double sleepInt) {
        super(lightLine);
        this.sleepInt = sleepInt;
    }

    public static String formatString(double sleep) {
        return "sleep " + sleep;
    }

}
