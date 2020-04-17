package com.github.fernthedev.terminal.server.backend;

import com.github.fernthedev.lightchat.core.data.LightCandidate;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.terminal.core.CandidateUtil;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;


public class AutoCompleteHandler extends ClientAutoCompleteHandler implements Completer {

    public AutoCompleteHandler(Server server) {
        super(server);
    }


    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
//        Server.getLogger().info("Handled line");


        List<LightCandidate> candidateList = handleLine(line.words());

        List<Candidate> convertedCandidate = new ArrayList<>();

        for(LightCandidate lightCandidate : candidateList) {
            convertedCandidate.add(CandidateUtil.toCandidate(lightCandidate));
        }

        if(!candidateList.isEmpty()) {
            candidates.addAll(convertedCandidate);
        }
    }
}
