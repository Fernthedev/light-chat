package com.github.fernthedev.lightchat.server.terminal.command;

import com.github.fernthedev.lightchat.server.SenderInterface;

import java.util.LinkedList;
import java.util.List;

public interface TabExecutor {

    /**
     * Returns a list of completions based on the arguments given
     * @param sender
     * @param args
     * @return
     */
    List<String> getCompletions(SenderInterface sender, LinkedList<String> args);


}
