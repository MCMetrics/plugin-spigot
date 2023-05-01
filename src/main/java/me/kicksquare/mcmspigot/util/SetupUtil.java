package me.kicksquare.mcmspigot.util;

import de.leonhard.storage.Config;
import me.kicksquare.mcmspigot.MCMSpigot;

public class SetupUtil {

    private static final MCMSpigot plugin = MCMSpigot.getPlugin();
    static Config mainConfig = plugin.getMainConfig();
    static Config dataConfig = plugin.getDataConfig();
    static Config bansConfig = plugin.getBansConfig();

    public static boolean isSetup() {
        return dataConfig.getBoolean("setup-complete") &&
                !(mainConfig.getString("server_id").equals("") || mainConfig.getString("uid") == "");
    }

    public static boolean shouldRecordSessions() {
        return isSetup() && dataConfig.getBoolean("record-sessions");
    }

    public static boolean shouldRecordPings() {
        return isSetup() && dataConfig.getBoolean("record-pings");
    }

    public static boolean shouldRecordPayments() {
        return isSetup() && dataConfig.getBoolean("record-payments");
    }

    public static boolean shouldExecuteExperiments() {
        return isSetup() && dataConfig.getBoolean("execute-experiments");
    }

    public static boolean shouldCheckGlobalBans() {
        return isSetup() && dataConfig.getBoolean("global-bans") && bansConfig.getBoolean("enabled");
    }
}