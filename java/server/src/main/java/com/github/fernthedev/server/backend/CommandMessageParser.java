package com.github.fernthedev.server.backend;

import com.github.fernthedev.server.CommandWorkerThread;
import com.github.fernthedev.server.Console;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.command.Command;
import com.github.fernthedev.server.command.CommandSender;
import com.github.fernthedev.server.event.Listener;
import com.github.fernthedev.server.event.chat.ChatEvent;
import com.github.fernthedev.core.ColorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.github.fernthedev.server.CommandWorkerThread.commandList;

@RequiredArgsConstructor
public class CommandMessageParser implements Listener {

    @NonNull
    private Server server;

    public static void onCommand(ChatEvent e) {
        CommandSender sender = e.getSender();

        if(e.isCancelled()) return;

        Runnable runnable;

        Runnable messageRunnable = () -> handleCommand(sender, e.getMessage());
        if(e.getSender() instanceof Console) {
            runnable = messageRunnable;
        }else{
            if(e.isCommand()) {
                runnable = messageRunnable;
            }else{
                runnable = () -> handleMessage(sender,e.getMessage());
            }
        }

        if(e.isAsynchronous()) {
            new Thread(runnable).start();
        }else{
            runnable.run();
        }

    }

    private static void handleCommand(CommandSender sender,String messageM) {
        String command = messageM;
        String[] checkmessage = command.split(" ", 2);
        List<String> messageword = new ArrayList<>();


        if (checkmessage.length > 1) {
            String[] messagewordCheck = command.split(" ");

            int index = 0;


            for (String message : messagewordCheck) {
                if (message == null) continue;

                message = message.replaceAll(" {2}", " ");

                index++;
                if (index == 1 || message.equals("")) continue;


                messageword.add(message);
            }
        }

        command = checkmessage[0];

        boolean found = false;


        command = command.replaceAll(" {2}", " ");

        if (!command.equals("")) {
            try {

                for (Command serverCommand : commandList) {
                    if (serverCommand.getName().equalsIgnoreCase(command)) {
                        found = true;
                        String[] args = new String[messageword.size()];
                        args = messageword.toArray(args);


                        new CommandWorkerThread(sender, serverCommand, args).run();

                        break;
                    }
                }
            } catch (Exception e) {
                Server.getLogger().error(e.getMessage(), e.getCause());
            }

            if (!found) {
                sender.sendMessage(ColorCode.RED + "No such command found");
            }
        }
    }

    private static void handleMessage(CommandSender sender, String message) {
        Server.sendMessage("[" + sender.getName() + "] :" + message);
    }

}
