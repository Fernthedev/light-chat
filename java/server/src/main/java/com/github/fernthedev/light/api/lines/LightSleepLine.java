package com.github.fernthedev.light.api.lines;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class LightSleepLine extends LightLine {

    private double sleepInt;

    public LightSleepLine(@NonNull String line, @NonNull int lineNumber,@NonNull double sleepInt) {
        super(line, lineNumber);
        this.sleepInt = sleepInt;
    }

    public LightSleepLine(LightLine lightLine,@NonNull double sleepInt) {
        super(lightLine);
        this.sleepInt = sleepInt;
    }

}
