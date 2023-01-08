package com.github.fernthedev.terminal.core

import com.github.fernthedev.lightchat.core.data.LightCandidate
import org.jline.reader.Candidate

object CandidateUtil {
    @JvmStatic
    fun toCandidate(lightCandidate: LightCandidate): Candidate {
        assert(lightCandidate.value != null)
        return Candidate(
            lightCandidate.value,
            lightCandidate.displ,
            lightCandidate.group,
            lightCandidate.descr,
            lightCandidate.suffix,
            lightCandidate.key,
            lightCandidate.isComplete
        )
    }
}