package com.github.fernthedev.server.command;

import com.github.fernthedev.config.common.Config;
import com.github.fernthedev.core.ColorCode;
import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.settings.Settings;

import java.util.List;
import java.util.Map;

public class SettingsCommand extends Command {


    private final Server server;

    public SettingsCommand(Server server) {
        super("settings");
        this.server = server;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("Possible args: set,get,reload,save, list");
        } else {
            boolean authenticated = server.getAuthenticationManager().authenticate(sender);
            if (authenticated) {
                long timeStart;
                long timeEnd;
                long timeElapsed;
                String arg = args[0];
                Config<Settings> settingsManager = server.getSettingsManager();

                switch (arg.toLowerCase()) {
                    case "set":
                        if (args.length > 2) {
                            String settingName = args[1];
                            String newValue = args[2];

                            try {
                                settingsManager.getConfigData().setValue(settingName, newValue);
                                sender.sendMessage(ColorCode.GREEN + "Set " + settingName + " to " + newValue);
                                sender.sendMessage(ColorCode.YELLOW + "Some settings will require a restart to take effect.");
                            } catch (ClassCastException | IllegalArgumentException e) {
                                sender.sendMessage("Error: " + e.getMessage() + " {" + e.getClass().getName() + "}");
                            }
                        } else sender.sendMessage("Usage: settings set {name} {newvalue}");
                        break;

                    case "get":
                        if (args.length > 1) {
                            String key = args[1];

                            try {
                                Object value = settingsManager.getConfigData().getValue(key);
                                sender.sendMessage("Value of " + key + ": " + value);
                            } catch (ClassCastException | IllegalArgumentException e) {
                                sender.sendMessage("Error:" + e.getMessage());
                            }
                        } else sender.sendMessage("Usage: settings get {serverKey}");
                        break;

                    case "reload":
                        sender.sendMessage("Reloading.");
                        timeStart = System.nanoTime();
                        settingsManager.save();

                        settingsManager.load();
                        timeEnd = System.nanoTime();
                        timeElapsed = (timeEnd - timeStart) / 1000000;
                        sender.sendMessage("Finished reloading. Took " + timeElapsed + "ms");
                        break;

                    case "list":
                        sender.sendMessage("Possible setting names : {possible values, empty if any are allowed}");

                        Map<String, List<String>> nameValueMap = settingsManager.getConfigData().getSettingValuesAsync(true);

                        for(String settingName : nameValueMap.keySet()) {
                            List<String> possibleValues = nameValueMap.get(settingName);
                            sender.sendMessage(settingName + " : " + possibleValues.toString());
                        }

                        break;

                    case "save":
                        sender.sendMessage("Saving.");
                        timeStart = System.nanoTime();
                        settingsManager.save();
                        timeEnd = System.nanoTime();
                        timeElapsed = (timeEnd - timeStart) / 1000000;
                        sender.sendMessage("Finished saving. Took " + timeElapsed + "ms");
                        break;
                    default:
                        sender.sendMessage("No such argument found " + arg + " found");
                        break;
                }
            }
        }
    }
}
