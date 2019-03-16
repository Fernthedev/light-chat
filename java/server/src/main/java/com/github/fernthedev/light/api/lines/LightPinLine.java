package com.github.fernthedev.light.api.lines;

import com.pi4j.io.gpio.Pin;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class LightPinLine extends LightLine{
    @NonNull
    private Pin pin;
    @NonNull
    private boolean toggle;

    public LightPinLine(@NonNull String line, @NonNull int lineNumber, Pin pin, boolean toggle) {
        super(line, lineNumber);
        this.pin = pin;
        this.toggle = toggle;
    }

    public LightPinLine(LightLine lightLine,Pin pin,boolean toggle) {
        super(lightLine);
        this.pin = pin;
        this.toggle = toggle;
    }
}
