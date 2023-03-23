package me.kicksquare.mcmspigot.util;

import me.kicksquare.mcmspigot.MCMSpigot;

public class LoggerUtil {

    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    public static void warning(String message) {
        plugin.getLogger().warning(message);
    }

    public static void info(String message) {
        plugin.getLogger().info(message);
    }

    public static void debug(String message) {
        if (plugin.getMainConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public static void severe(String message) {
        plugin.getLogger().severe(message);
    }

}