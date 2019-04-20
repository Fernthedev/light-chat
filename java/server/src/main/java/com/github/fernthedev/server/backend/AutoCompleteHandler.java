package com.github.fernthedev.server.backend;

import com.github.fernthedev.packets.LightCandidate;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.TabExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class AutoCompleteHandler implements Completer {
    @NonNull
    private Server server;


    public List<LightCandidate> handleLine(List<String> words) {
        List<LightCandidate> candidates = new ArrayList<>();



        if(words.size() == 1) {
            String c = words.get(0);
            String prefix = "";
            if(c.startsWith("/")) {
                prefix = "/";
                c = c.substring(1);
            }

            final String finishString = c;


            List<Command> completions = server.getCommands().stream().filter(
                    item -> item.getCommandName().startsWith(finishString)).collect(Collectors.toList());


            for (Command command : completions) {
                String commandName = command.getCommandName();

                String newString = prefix + commandName;

                candidates.add(new LightCandidate(AttributedString.stripAnsi(newString), newString, null, null, null, null, true));
            }
        }else{
            String c = words.get(0);

            Command curCommand = null;

            if(c.startsWith("/")) {
                c = c.substring(1);
            }

            for(Command command : server.getCommands()) {
                if(command.getCommandName().equalsIgnoreCase(c)) {
                    curCommand = command;
                    break;
                }
            }

            if(curCommand instanceof TabExecutor) {
                TabExecutor tabExecutor = (TabExecutor) curCommand;

                List<String> args = new ArrayList<>(words);

                if(args.isEmpty()) return candidates;

                args.remove(0);

                @NonNull List<String> completions = tabExecutor.getCompletions(args.toArray(new String[0]));

                for (String string : completions) {
                    candidates.add(new LightCandidate(AttributedString.stripAnsi(string), string, null, null, null, null, true));
                }
            }else{
                return new ArrayList<>();
            }
        }

        return candidates;
    }


    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

        List<LightCandidate> candidateList = handleLine(line.words());

        List<Candidate> candidateList1 = new ArrayList<>();

        for(LightCandidate lightCandidate : candidateList) {
            candidateList1.add(lightCandidate.toCandidate());
        }

        if(!candidateList.isEmpty()) {
            candidates.addAll(candidateList1);
        }
    }
}
