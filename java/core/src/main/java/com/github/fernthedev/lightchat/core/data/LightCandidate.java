package com.github.fernthedev.core.data;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class LightCandidate implements Serializable {

    private final String value;
    private final String displ;
    private final String group;
    private final String descr;
    private final String suffix;
    private final String key;
    private final boolean complete;

    /**
     * Simple constructor with only a single String as an argument.
     *
     * @param value the candidate
     */
    public LightCandidate(String value) {
        this(value, value, null, null, null, null, true);
    }

    /**
     * Constructs a new Candidate.
     *
     * @param value the value
     * @param displ the display string
     * @param group the group
     * @param descr the description
     * @param suffix the suffix
     * @param key the key
     * @param complete the complete flag
     */
    public LightCandidate(String value, String displ, String group, String descr, String suffix, String key, boolean complete) {
        Objects.requireNonNull(value);
        this.value = value;
        this.displ = displ;
        this.group = group;
        this.descr = descr;
        this.suffix = suffix;
        this.key = key;
        this.complete = complete;
    }
}
