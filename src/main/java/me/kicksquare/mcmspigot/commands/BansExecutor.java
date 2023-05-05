package me.kicksquare.mcmspigot.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.bans.GlobalBansResponseEntry;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;

import static me.kicksquare.mcmspigot.util.ColorUtil.colorize;

public class BansExecutor {
    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    // returns false if the help message should be shown
    public static boolean executeBansSubcommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.hasPermission("mcmetrics.bans")) {
                p.sendMessage(colorize("&cYou do not have permission to use this command."));
                return true;
            }
        }

        if (args[1].equalsIgnoreCase("add")) {
            if (args.length < 5) {
                sender.sendMessage(colorize("&cUsage: &7 • &b/mcmetrics bans add <player name/uuid> <reason> <evidence> &7- Bans a player using MCMetrics Global Bans"));
                return true;
            }

            String player = args[2];
            String reason = args[3];
            // evidence is args[4] and beyond
            String evidence = "";
            for (int i = 4; i < args.length; i++) {
                evidence += args[i] + " ";
            }
            // remove the space at the end
            evidence = evidence.substring(0, evidence.length() - 1);

            // make sure reason is valid
            if (!(
                    reason.equalsIgnoreCase("DUPE") ||
                            reason.equalsIgnoreCase("LAG") ||
                            reason.equalsIgnoreCase("DISCRIMINATION") ||
                            reason.equalsIgnoreCase("BOTTING")
            )) {
                sender.sendMessage(colorize("&cInvalid reason. Valid reasons: DUPE, LAG, DISCRIMINATION, BOTTING"));
                return true;
            }

            // make a request to the api to get a list of bans for this player
            final String bodyString = "{\"username\": \"" + player + "\", \"reason\": \"" + reason.toUpperCase() + "\", \"evidence\": \"" + evidence + "\"}";
            LoggerUtil.debug("Adding ban for player " + player + ". Body: " + bodyString);
            String finalEvidence = evidence;
            HttpUtil.makeAsyncPostRequest("api/bans/banPlayerFromServer", bodyString, HttpUtil.getAuthHeadersFromConfig())
                    .thenAccept(response -> {
                        if (response == null) {
                            LoggerUtil.warning("Failed to add ban for player " + player + ". Response was null.");
                            return;
                        }

                        sender.sendMessage(colorize("&aSuccessfully banned player &a&l" + player + "&a for reason &a&l" + reason + "&a with evidence &a" + finalEvidence
                                + "&7. Thank you for contributing to the MCMetrics Global Bans database!"));
                    });
            return true;
        } else if (args[1].equalsIgnoreCase("lookup")) {
            if (args.length != 3) {
                sender.sendMessage(colorize("&cUsage: &7 • &b/mcmetrics bans lookup <player name/uuid> &7- Looks up a player's bans using MCMetrics Global Bans"));
                return true;
            }

            String player = args[2];

            // make a request to the api to get a list of bans for this player
            final String bodyString = "{\"uuid\": \"" + player + "\"}";
            LoggerUtil.debug("Checking global bans for player " + player + ". Body: " + bodyString);
            sender.sendMessage(colorize("&aChecking global bans for player &a&l" + player + "&a..."));
            HttpUtil.makeAsyncPostRequest("api/bans/serverPlayerLookup", bodyString, HttpUtil.getAuthHeadersFromConfig())
                    .thenAccept(response -> {
                        if (response == null) {
                            LoggerUtil.warning("Failed to check global bans for player " + player + ". Response was null.");
                            return;
                        }

                        try {
                            ObjectMapper mapper = new ObjectMapper();

                            GlobalBansResponseEntry[] bans = mapper.readValue(response, GlobalBansResponseEntry[].class);

                            if (bans.length == 0) {
                                sender.sendMessage(colorize("&7Player &a&l" + player + "&7 has no bans on record."));
                                return;
                            }

                            sender.sendMessage(colorize("&7Player &a&l" + player + "&7 has the following bans on record:"));
                            for (GlobalBansResponseEntry ban : bans) {
                                sender.sendMessage(colorize("&7 • &a&l" + ban.ban_reason + "&7 with evidence &a&l" + ban.evidence + "&7 by &a&l" + ban.global_bans_identifier + "&7 on &a&l" + prettifyDate(ban.ban_time)));
                            }

                        } catch (JsonProcessingException exception) {
                            // if the message contains "Invalid user or server id", don't spam the console and just send one custom error
                            if (response.contains("Invalid user or server id")) {
                                LoggerUtil.severe("Error occurred while fetching player ban status: Invalid user or server id");
                                LoggerUtil.severe("Make sure your server is properly set up by running /mcmetrics setup");
                                return;
                            }
                            if (plugin.getMainConfig().getBoolean("debug")) {
                                LoggerUtil.severe("Error occurred while fetching player ban status: " + exception.getMessage());
                                exception.printStackTrace();
                            }
                        }
                    });

            return true;
        }

        return false;
    }

    private static String prettifyDate(Date date) {
        // "01/01/2021"
        return date.getMonth() + "/" + date.getDay() + "/" + date.getYear();
    }
}
