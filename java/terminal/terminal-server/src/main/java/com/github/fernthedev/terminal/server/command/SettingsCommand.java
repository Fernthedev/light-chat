package com.github.fernthedev.terminal.server.command;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.lightchat.core.ColorCode;
import com.github.fernthedev.lightchat.server.SenderInterface;
import com.github.fernthedev.lightchat.server.Server;
import com.github.fernthedev.lightchat.server.settings.ServerSettings;
import com.github.fernthedev.terminal.server.ServerTerminal;

import java.util.List;
import java.util.Map;

public class SettingsCommand extends Command {


    private final Server server;

    public SettingsCommand(Server server) {
        super("settings");
        this.server = server;
    }

    @Override
    public void onCommand(SenderInterface sender, String[] args) {

        if (args.length == 0) {
            ServerTerminal.sendMessage(sender, ("Possible args: set,get,reload,save, list"));
        } else {
            boolean authenticated = ServerTerminal.getAuthenticationManager().authenticate(sender);
            if (authenticated) {
                long timeStart;
                long timeEnd;
                long timeElapsed;
                String arg = args[0];
                Config<ServerSettings> settingsManager = server.getSettingsManager();

                switch (arg.toLowerCase()) {
                    case "set":
                        if (args.length > 2) {
                            String settingName = args[1];
                            String newValue = args[2];

                            try {
                                settingsManager.getConfigData().setValue(settingName, newValue);
                                ServerTerminal.sendMessage(sender, ColorCode.GREEN + "Set " + settingName + " to " + newValue);
                                ServerTerminal.sendMessage(sender, ColorCode.YELLOW + "Some settings will require a restart to take effect.");
                            } catch (ClassCastException | IllegalArgumentException e) {
                                ServerTerminal.sendMessage(sender, "Error: " + e.getMessage() + " {" + e.getClass().getName() + "}");
                            }
                        } else ServerTerminal.sendMessage(sender, "Usage: settings set {name} {newvalue}");
                        break;

                    case "get":
                        if (args.length > 1) {
                            String key = args[1];

                            try {
                                Object value = settingsManager.getConfigData().getValue(key);
                                ServerTerminal.sendMessage(sender, "Value of " + key + ": " + value);
                            } catch (ClassCastException | IllegalArgumentException e) {
                                ServerTerminal.sendMessage(sender, "Error:" + e.getMessage());
                            }
                        } else ServerTerminal.sendMessage(sender, "Usage: settings get {serverKey}");
                        break;

                    case "reload":
                        ServerTerminal.sendMessage(sender, "Reloading.");
                        timeStart = System.nanoTime();
                        settingsManager.save();

                        settingsManager.load();
                        timeEnd = System.nanoTime();
                        timeElapsed = (timeEnd - timeStart) / 1000000;
                        ServerTerminal.sendMessage(sender, "Finished reloading. Took " + timeElapsed + "ms");
                        break;

                    case "list":
                        ServerTerminal.sendMessage(sender, "Possible setting names : {possible values, empty if any are allowed}");

                        try {
                            Map<String, List<String>> nameValueMap = settingsManager.getConfigData().getSettingValues(true);

                            for (String settingName : nameValueMap.keySet()) {
                                List<String> possibleValues = nameValueMap.get(settingName);
                                ServerTerminal.sendMessage(sender, settingName + " : " + possibleValues.toString());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;

                    case "save":
                        ServerTerminal.sendMessage(sender, "Saving.");
                        timeStart = System.nanoTime();
                        settingsManager.save();
                        timeEnd = System.nanoTime();
                        timeElapsed = (timeEnd - timeStart) / 1000000;
                        ServerTerminal.sendMessage(sender, "Finished saving. Took " + timeElapsed + "ms");
                        break;
                    default:
                        ServerTerminal.sendMessage(sender, "No such argument found " + arg + " found");
                        break;
                }
            }
        }
    }
}
