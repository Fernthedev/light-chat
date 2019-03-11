package com.github.fernthedev.light.exceptions;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LightLine {

    @NonNull
    private final String line;

    @NonNull
    private final int lineNumber;





}
