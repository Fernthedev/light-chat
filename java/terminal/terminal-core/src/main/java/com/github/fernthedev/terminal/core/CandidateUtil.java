package com.github.fernthedev.terminal.core;

import com.github.fernthedev.lightchat.core.data.LightCandidate;
import org.jline.reader.Candidate;

public class CandidateUtil {

    public static Candidate toCandidate(LightCandidate lightCandidate) {
        assert lightCandidate.value != null;
        return new Candidate(lightCandidate.value,lightCandidate.displ,lightCandidate.group,lightCandidate.descr,lightCandidate.suffix,lightCandidate.key,lightCandidate.isComplete());
    }

}
