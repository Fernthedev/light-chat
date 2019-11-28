package com.github.fernthedev.server.command;

import java.util.List;

public interface TabExecutor {

    List<String> getCompletions(String[] args);


}
