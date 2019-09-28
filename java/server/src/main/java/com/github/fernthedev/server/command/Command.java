package com.github.fernthedev.server.command;

import lombok.*;

@RequiredArgsConstructor
public abstract class Command {

    @NonNull
    @Getter
    private String name;

    @Getter
    @Setter
    private String usage = "";

    public abstract void onCommand(CommandSender sender,String[] args);
}
