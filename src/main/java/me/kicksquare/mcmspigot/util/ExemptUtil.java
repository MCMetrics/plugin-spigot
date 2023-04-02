package me.kicksquare.mcmspigot.util;

import me.kicksquare.mcmspigot.MCMSpigot;
import org.bukkit.entity.Player;

import java.util.List;

public class ExemptUtil {
    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    public static boolean isExempt(Player player) {
        String username = player.getName();
        String uuid = player.getUniqueId().toString();

        List<String> exemptPlayers = plugin.getMainConfig().getStringList("exempt-players");

        return exemptPlayers.contains(username) || exemptPlayers.contains(uuid);
    }
}
