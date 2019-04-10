package com.github.fernthedev.client.backend;

import com.github.fernthedev.packets.AutoCompletePacket;
import com.github.fernthedev.client.Client;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

@RequiredArgsConstructor
public class AutoCompleteHandler implements Completer {

    @NonNull
    private Client client;

    private static List<Candidate> candidateList;

    public static void addCandidates(List<Candidate> candidates) {
        if(candidateList != null) {
            candidateList.addAll(candidates);
        }
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
        if(candidateList != null)
        candidateList.clear();

        candidateList = candidates;
        AutoCompletePacket autoCompletePacket = new AutoCompletePacket(line.words());
        client.getClientThread().sendObject(autoCompletePacket);
    }
}
