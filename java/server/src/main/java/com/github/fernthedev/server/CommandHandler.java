package com.github.fernthedev.server;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements Runnable {

    private final Command serverCommand;
    private final String[] args;
    private final CommandSender commandSender;

    static List<Command> commandList = new ArrayList<>();

    public CommandHandler(CommandSender commandSender,Command command, String[] args) {
        this.serverCommand = command;
        this.args = args;
        this.commandSender = commandSender;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        serverCommand.onCommand(commandSender,args);

        Server.closeThread(Thread.currentThread());
    }
}
