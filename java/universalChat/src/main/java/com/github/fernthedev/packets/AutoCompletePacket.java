package com.github.fernthedev.packets;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class AutoCompletePacket extends Packet {

    private List<LightCandidate> candidateList = new ArrayList<>();

    @NonNull
    private List<String> words;
}
