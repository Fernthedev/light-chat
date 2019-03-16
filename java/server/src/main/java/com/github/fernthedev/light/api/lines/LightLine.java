package com.github.fernthedev.light.api.lines;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode()
@RequiredArgsConstructor
public class LightLine {

    @NonNull
    private final String line;

    private final int lineNumber;

    public LightLine(LightLine lightLine) {
        this.line = lightLine.getLine();
        this.lineNumber = lightLine.getLineNumber();
    }


}
