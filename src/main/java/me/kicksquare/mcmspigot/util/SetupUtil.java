package me.kicksquare.mcmspigot.util;

import me.kicksquare.mcmspigot.MCMSpigot;
import org.bukkit.configuration.file.FileConfiguration;

public class SetupUtil {

    private static MCMSpigot plugin = MCMSpigot.getPlugin();
    private static FileConfiguration config = plugin.getConfig();


    public static boolean isSetup() {
        return config.getBoolean("setup-complete") &&
                !(config.getString("server_id").equals("") || config.getString("uid") == "");
    }

    public static boolean shouldRecordSessions() {
        return isSetup() && config.getBoolean("record-sessions");
    }

    public static boolean shouldRecordPings() {
        return isSetup() && config.getBoolean("record-pings");
    }

    public static boolean shouldRecordPayments() {
        return isSetup() && config.getBoolean("record-payments");
    }

    public static boolean shouldExecuteExperiments() {
        return isSetup() && config.getBoolean("execute-experiments");
    }

}
