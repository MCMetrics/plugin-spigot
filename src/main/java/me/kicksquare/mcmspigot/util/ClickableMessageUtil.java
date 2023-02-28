package me.kicksquare.mcmspigot.util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import static me.kicksquare.mcmspigot.util.ColorUtil.colorize;

public class ClickableMessageUtil {
    /**
     * Sends a clickable message to a player that runs a command when clicked
     *
     * @param message The clickable message
     * @param command The command without the slash to make the user perform
     * @param player  player to send to
     */
    public static void sendClickableCommand(Player player, String message, String command) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(colorize(message)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(colorize("&eClick to run command!"))));

        player.spigot().sendMessage(component);
    }
}
