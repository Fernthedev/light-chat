package com.github.fernthedev.server.command;

import org.jetbrains.annotations.NotNull;

public abstract class Command {
    private String command;

    private String usage = "";

    public String getCommandName() {
        return command;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public Command(@NotNull String command) {
        this.command = command;
    }

    public abstract void onCommand(CommandSender sender,String[] args);
}