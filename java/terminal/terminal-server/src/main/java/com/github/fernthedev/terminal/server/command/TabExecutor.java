package com.github.fernthedev.terminal.server.command;

import java.util.List;

public interface TabExecutor {

    List<String> getCompletions(String[] args);


}
