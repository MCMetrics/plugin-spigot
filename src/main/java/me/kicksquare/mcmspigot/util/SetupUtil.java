package me.kicksquare.mcmspigot.util;

import me.kicksquare.mcmspigot.MCMSpigot;
import org.bukkit.configuration.file.FileConfiguration;

public class SetupUtil {

    private static MCMSpigot plugin = MCMSpigot.getPlugin();

    public static boolean isSetup() {
        FileConfiguration config = plugin.getConfig();

        return config.getBoolean("setup-complete") &&
                !(config.getString("server_id").equals("") || config.getString("uid") == "");
    }

    public static boolean shouldRecordSessions() {
        FileConfiguration config = plugin.getConfig();
        return isSetup() && config.getBoolean("record-sessions");
    }

    public static boolean shouldRecordPings() {
        FileConfiguration config = plugin.getConfig();
        return isSetup() && config.getBoolean("record-pings");
    }

    public static boolean shouldRecordPayments() {
        FileConfiguration config = plugin.getConfig();
        return isSetup() && config.getBoolean("record-payments");
    }

    public static boolean shouldExecuteExperiments() {
        FileConfiguration config = plugin.getConfig();
        return isSetup() && config.getBoolean("execute-experiments");
    }

}
