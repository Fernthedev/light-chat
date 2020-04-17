package com.github.fernthedev.terminal.core;

import com.github.fernthedev.lightchat.core.data.LightCandidate;
import org.jline.reader.Candidate;

public class CandidateUtil {

    public static Candidate toCandidate(LightCandidate lightCandidate) {
        return new Candidate(lightCandidate.getValue(),lightCandidate.getDispl(),lightCandidate.getGroup(),lightCandidate.getDescr(),lightCandidate.getSuffix(),lightCandidate.getKey(),lightCandidate.isComplete());
    }

}
