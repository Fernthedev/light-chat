package com.github.fernthedev.server.backend;

import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.TabExecutor;
import lombok.NonNull;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;


public class AutoCompleteHandler implements Completer {
    private Server server;


    public AutoCompleteHandler(Server server) {
        this.server = server;
    }

    public List<Candidate> handleLine(ParsedLine line) {
        List<Candidate> candidates = new ArrayList<>();

        if(line.words().size() == 1) {
            for (Command command : server.getCommands()) {
                String string = command.getCommandName();
                candidates.add(new Candidate(AttributedString.stripAnsi(string), string, null, null, null, null, true));
            }
        }else{
            String c = line.words().get(0);
            Command curCommand = null;

            for(Command command : server.getCommands()) {
                if(command.getCommandName().equalsIgnoreCase(c)) {
                    curCommand = command;
                    break;
                }
            }

            if(curCommand instanceof TabExecutor) {
                TabExecutor tabExecutor = (TabExecutor) curCommand;

                List<String> args = new ArrayList<>(line.words());

                if(args.isEmpty()) return candidates;

                args.remove(0);

                @NonNull List<String> completions = tabExecutor.getCompletions(args.toArray(new String[0]));

                for (String string : completions) {
                    candidates.add(new Candidate(AttributedString.stripAnsi(string), string, null, null, null, null, true));
                }
            }
        }

        return candidates;
    }


    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        List<Candidate> candidateList = handleLine(line);

        if(!candidateList.isEmpty())
        candidates.addAll(candidateList);
    }
}
