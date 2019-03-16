package com.github.fernthedev.light.api.lines;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class LightPrintLine extends LightLine {
    private String print;

    public LightPrintLine(@NonNull String line, @NonNull int lineNumber,@NonNull String print) {
        super(line, lineNumber);
        this.print = print;
    }

    public LightPrintLine(LightLine lightLine,@NonNull String print) {
        super(lightLine);
        this.print = print;
    }
}
