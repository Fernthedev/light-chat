package com.github.fernthedev;

import com.github.fernthedev.packets.Packet;
import lombok.*;
import org.jline.reader.Candidate;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class AutoCompletePacket extends Packet {

    private List<Candidate> candidateList = new ArrayList<>();

    @NonNull
    private ParsedLine line;
}
