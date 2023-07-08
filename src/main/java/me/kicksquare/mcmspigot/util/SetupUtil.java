package me.kicksquare.mcmspigot.util;

import me.kicksquare.mcmspigot.MCMSpigot;

public class SetupUtil {

    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    public static boolean isSetup() {
        return plugin.getDataConfig().getBoolean("setup-complete") &&
                !(plugin.getMainConfig().getString("server_id").equals("") || plugin.getMainConfig().getString("uid").equals(""));
    }

    public static boolean shouldRecordSessions() {
        return true;
    }

    public static boolean shouldRecordPings() {
        return isSetup() && plugin.getDataConfig().getBoolean("record-pings");
    }

    public static boolean shouldRecordPayments() {
        return isSetup() && plugin.getDataConfig().getBoolean("record-payments");
    }

    public static boolean shouldExecuteExperiments() {
        return isSetup() && plugin.getDataConfig().getBoolean("execute-experiments");
    }

    public static boolean shouldCheckGlobalBans() {
        return isSetup() && plugin.getDataConfig().getBoolean("global-bans") && plugin.getBansConfig().getBoolean("enabled");
    }
}