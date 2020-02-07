package com.github.fernthedev.terminal.client;

import com.github.fernthedev.client.Client;
import com.github.fernthedev.core.packets.AutoCompletePacket;
import com.github.fernthedev.core.data.LightCandidate;
import com.github.fernthedev.terminal.core.CandidateUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AutoCompleteHandler implements Completer {

    @NonNull
    private Client client;

    private List<Candidate> candidateList = new ArrayList<>();

    private boolean keepCheck;

    public void addCandidates(List<LightCandidate> candidates) {
        if(candidateList != null) {
            List<Candidate> candidateList1 = new ArrayList<>();
            for(LightCandidate lightCandidate : candidates) {
                candidateList1.add(CandidateUtil.toCandidate(lightCandidate));
            }
            candidateList.addAll(candidateList1);
        }
        keepCheck = false;
    }

    /**
     * Populates <i>candidates</i> with a list of possible completions for the <i>command line</i>.
     * <p>
     * The list of candidates will be sorted and filtered by the LineReader, so that
     * the list of candidates displayed to the user will usually be smaller than
     * the list given by the completer.  Thus it is not necessary for the completer
     * to do any matching based on the current buffer.  On the contrary, in order
     * for the typo matcher to work, all possible candidates for the word being
     * completed should be returned.
     *
     * @param reader     The line reader
     * @param line       The parsed command line
     * @param candidates The {@link List} of candidates to populate
     */
    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if(candidateList != null) {
            candidateList.clear();
        }

        AutoCompletePacket autoCompletePacket = new AutoCompletePacket(line.words());
        client.sendObject(autoCompletePacket);

        keepCheck = true;
        while (keepCheck) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        candidates.addAll(candidateList);


    }
}
