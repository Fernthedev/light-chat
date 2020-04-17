package com.github.fernthedev.lightchat.core;

import lombok.*;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionData {

    @Getter
    @Nullable
    private VariablesJSON variablesJSON;

    @NonNull
    private DefaultArtifactVersion version;

    @NonNull
    private DefaultArtifactVersion minVersion;

    public VersionData(VariablesJSON variablesJSON) {
        this(variablesJSON.getVersion(), variablesJSON.getMinVersion());
        this.variablesJSON = variablesJSON;
    }

    public VersionData(String version, String minVersion) {
        this.version = new DefaultArtifactVersion(version);
        this.minVersion = new DefaultArtifactVersion(minVersion);
    }






}
