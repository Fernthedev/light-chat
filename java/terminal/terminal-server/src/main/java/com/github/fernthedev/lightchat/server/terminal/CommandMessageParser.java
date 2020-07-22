package com.github.fernthedev.lightchat.server.terminal;

import com.github.fernthedev.lightchat.core.ColorCode;
import com.github.fernthedev.lightchat.core.api.event.api.Listener;
import com.github.fernthedev.lightchat.server.Console;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.terminal.command.Command;
import com.github.fernthedev.lightchat.server.terminal.events.ChatEvent;
import com.github.fernthedev.lightchat.server.terminal.exception.InvalidCommandArgumentException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CommandMessageParser implements Listener {

    @NonNull
    private final Server server;

    public void onCommand(ChatEvent e) {
        SenderInterface sender = e.getSender();

        if (e.isCancelled()) return;

        Runnable runnable;

        Runnable commandRunnable = () -> handleCommand(sender, e.getMessage()); // Just a static identifier
        Runnable messageRunnable = () -> handleMessage(sender, e.getMessage()); // Just a static identifier

        if (e.getSender() instanceof Console) {
            runnable = commandRunnable;
        } else {
            if (e.isCommand()) {
                runnable = commandRunnable;
            } else {
                runnable = messageRunnable;
            }
        }

        if (e.isAsynchronous()) {
            server.getExecutorService().submit(runnable);
        } else {
            runnable.run();
        }

    }

    private void handleCommand(SenderInterface sender, String command) {
        String[] splitString = command.split(" ", 2);
        List<String> arguments = new ArrayList<>();


        if (splitString.length > 1) {
            String[] splitArgumentsCommand = command.split(" ");

            int index = 0;


            for (String message : splitArgumentsCommand) {
                if (message == null) continue;

                message = message.replaceAll(" {2}", " ");

                index++;
                if (index == 1 || message.equals("")) continue;


                arguments.add(message);
            }
        }

        String mainCommand = splitString[0];

        boolean found = false;


        mainCommand = mainCommand.replaceAll(" {2}", " ");

        if (!command.equals("")) {
            try {

                if (!(sender instanceof Console))
                    server.getLogger().info("[{}] /{}", sender.getName(), command);

                for (Command serverCommand : ServerTerminal.getCommands()) {
                    if (serverCommand.getName().equalsIgnoreCase(mainCommand)) {
                        found = true;
                        String[] args = new String[arguments.size()];
                        args = arguments.toArray(args);


                        new CommandWorkerThread(sender, serverCommand, args).run();

                        break;
                    }
                }
            } catch (InvalidCommandArgumentException e) {
                ServerTerminal.sendMessage(sender, ColorCode.RED + "Error: " + e.getMessage());
            } catch (Exception e) {
                server.getLogger().error(e.getMessage(), e);
                ServerTerminal.sendMessage(sender, ColorCode.RED + "Command exception occurred. Error: " + e.getMessage());
            }

            if (!found) {
                ServerTerminal.sendMessage(sender, (ColorCode.RED + "No such command found"));
            }
        }
    }

    private static void handleMessage(SenderInterface sender, String message) {
        ServerTerminal.broadcast("[" + sender.getName() + "]: " + message);
    }

}
