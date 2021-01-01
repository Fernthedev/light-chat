package com.github.fernthedev.lightchat.server.terminal.backend;

import com.github.fernthedev.lightchat.core.data.LightCandidate;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.terminal.ServerTerminal;
import com.github.fernthedev.lightchat.server.terminal.command.Command;
import com.github.fernthedev.lightchat.server.terminal.command.TabExecutor;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public class TabCompleteFinder {

    protected final Server server;

    public List<LightCandidate> handleLine(SenderInterface senderInterface, List<String> words) {
        List<LightCandidate> candidates = new ArrayList<>();

        if (words.isEmpty()) return candidates;

        if(words.size() == 1) {
            for (Command command : ServerTerminal.getCommands()) {
                String string = command.getName();
                candidates.add(new LightCandidate(string, string, null, null, null, null, true));
            }
        }else{
            List<String> args = new ArrayList<>(words);

            args.remove(0); // Remove the command as first argument

            String c = words.get(0);
            Command curCommand = null;

            for(Command command : ServerTerminal.getCommands()) {
                if(command.getName().equalsIgnoreCase(c)) {
                    curCommand = command;
                    break;
                }
            }

            if(curCommand instanceof TabExecutor) {
                TabExecutor tabExecutor = (TabExecutor) curCommand;

                @NonNull List<String> completions = tabExecutor.getCompletions(senderInterface, new LinkedList<>(args));

                for (String string : completions) {
                    candidates.add(new LightCandidate(string, string, null, null, null, null, true));
                }
            }
        }

        return candidates;
    }

}
