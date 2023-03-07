package me.kicksquare.mcmspigot.logging;

import me.kicksquare.mcmspigot.MCMSpigot;

public class Logger {

    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    public static void warning(String message) {
        plugin.getLogger().warning(message);
    }

    public static void info(String message) {
        plugin.getLogger().info(message);
    }

    public static void severe(String message) {
        plugin.getLogger().severe(message);
    }

}