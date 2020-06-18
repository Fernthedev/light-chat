package com.github.fernthedev.lightchat.server.terminal.command;

import java.util.List;

public interface TabExecutor {

    List<String> getCompletions(String[] args);


}
