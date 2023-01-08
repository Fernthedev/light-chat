package com.github.fernthedev.lightchat.server.terminal.command;

import com.github.fernthedev.lightchat.server.SenderInterface;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class Command {

    @NonNull
    @Getter
    private String name;

    @Getter
    @Setter
    private String usage = "";

    public abstract void onCommand(SenderInterface sender, String[] args);

    /**
     * Allows you to make autocomplete only suggest based off what is written
     * @param arg The argument currently used
     * @param possibilities All of the possibilities
     * @return The auto-complete possibilities
     */
    public List<String> search(String arg, List<String> possibilities) {
        List<String> newPos = new ArrayList<>();
        possibilities.forEach(s -> {
            if(s.startsWith(arg) || s.contains(arg)) {
                newPos.add(s);
            }
        });
        return newPos;
    }
}
