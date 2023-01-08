package com.github.fernthedev.lightchat.server.terminal.backend

import com.github.fernthedev.lightchat.server.Server
import com.github.fernthedev.terminal.core.CandidateUtil.toCandidate
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class AutoCompleteHandler(server: Server) : TabCompleteFinder(server), Completer {
    override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
//        Server.getLogger().info("Handled line");
        val candidateList = handleLine(server.console, line.words())
        val convertedCandidate: MutableList<Candidate> = ArrayList()
        for (lightCandidate in candidateList) {
            convertedCandidate.add(toCandidate(lightCandidate))
        }

        if (candidateList.isNotEmpty()) {
            candidates.addAll(convertedCandidate)
        }
    }
}