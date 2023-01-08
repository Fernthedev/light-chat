package com.github.fernthedev.lightchat.client.terminal

import com.github.fernthedev.lightchat.client.Client
import com.github.fernthedev.lightchat.core.data.LightCandidate
import com.github.fernthedev.terminal.core.CandidateUtil.toCandidate
import com.github.fernthedev.terminal.core.packets.AutoCompletePacket
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class AutoCompleteHandler(private val client: Client) : Completer {
    private val candidateList: MutableList<Candidate> = ArrayList()
    private var keepCheck = false
    fun addCandidates(candidates: List<LightCandidate>) {
        candidateList.addAll(candidates.map {
            toCandidate(it)
        })
        keepCheck = false
    }

    /**
     * Populates *candidates* with a list of possible completions for the *command line*.
     *
     *
     * The list of candidates will be sorted and filtered by the LineReader, so that
     * the list of candidates displayed to the user will usually be smaller than
     * the list given by the completer.  Thus it is not necessary for the completer
     * to do any matching based on the current buffer.  On the contrary, in order
     * for the typo matcher to work, all possible candidates for the word being
     * completed should be returned.
     *
     * @param reader     The line reader
     * @param line       The parsed command line
     * @param candidates The [List] of candidates to populate
     */
    override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
        candidateList.clear()

        val autoCompletePacket = AutoCompletePacket(line.words())
        client.sendObject(autoCompletePacket)
        keepCheck = true
        while (keepCheck) {
            try {
                Thread.sleep(10)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        candidates.addAll(candidateList)
    }
}