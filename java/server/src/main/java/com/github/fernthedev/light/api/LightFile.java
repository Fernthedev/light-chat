package com.github.fernthedev.light.api;

import com.github.fernthedev.light.api.lines.LightLine;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;

@Data
@RequiredArgsConstructor
public class LightFile {

    @NonNull
    private File file;

    @NonNull
    private List<LightLine> lineList;

}
