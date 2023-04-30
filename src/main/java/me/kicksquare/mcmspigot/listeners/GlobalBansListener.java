package me.kicksquare.mcmspigot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.bans.GlobalBansResponseEntry;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.SetupUtil;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalBansListener implements Listener {
    private final MCMSpigot plugin;

    public GlobalBansListener(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!SetupUtil.shouldCheckGlobalBans()) return;

        Player p = e.getPlayer();

        // make a request to the api to get a list of bans for this player
        final String bodyString = "{\"uuid\": \"" + p.getUniqueId() + "\"}";
        LoggerUtil.debug("Checking global bans for player " + p.getName() + ". Body: " + bodyString);
        HttpUtil.makeAsyncPostRequest("api/bans/serverPlayerLookup", bodyString, HttpUtil.getAuthHeadersFromConfig())
                .thenAccept(response -> {
                    if (response == null) {
                        LoggerUtil.warning("Failed to check global bans for player " + p.getName() + ". Response was null.");
                        return;
                    }

                    try {
                        ObjectMapper mapper = new ObjectMapper();

                        GlobalBansResponseEntry[] bans = mapper.readValue(response, GlobalBansResponseEntry[].class);

                        System.out.println(3);

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            System.out.println(5);
                            // only ban if there is at least one entry with a reason that is enabled in the config
                            boolean shouldBan = false;
                            String reason = "";
                            for (GlobalBansResponseEntry ban : bans) {
                                switch (ban.ban_reason) {
                                    case LAG:
                                        if (plugin.getBansConfig().getBoolean("lag")) {
                                            shouldBan = true;
                                            reason = ban.ban_reason.toString();
                                        }
                                        break;
                                    case DUPE:
                                        if (plugin.getBansConfig().getBoolean("dupe")) {
                                            shouldBan = true;
                                            reason = ban.ban_reason.toString();
                                        }
                                        break;
                                    case BOTTING:
                                        if (plugin.getBansConfig().getBoolean("botting")) {
                                            shouldBan = true;
                                            reason = ban.ban_reason.toString();
                                        }
                                        break;
                                    case DISCRIMINATION:
                                        if (plugin.getBansConfig().getBoolean("discrimination")) {
                                            shouldBan = true;
                                            reason = ban.ban_reason.toString();
                                        }
                                        break;
                                }
                            }

                            if (!shouldBan) {
                                LoggerUtil.debug("Skipping ban for player " + p.getName() + " because no enabled ban reasons were found.");
                                return;
                            }

                            System.out.println(4);

                            // run commands
                            List<String> commands = plugin.getBansConfig().getStringList("commands");
                            for (String rawCommand : commands) {
                                String command = replaceBansPlaceholders(rawCommand, p, reason);
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            }

                            // send discord webhook if enabled
                            if (plugin.getBansConfig().getBoolean("discord-webhook-enabled")) {
                                String title = replaceBansPlaceholders(plugin.getBansConfig().getString("discord-webhook-title"), p, reason);
                                String description = replaceBansPlaceholders(plugin.getBansConfig().getString("discord-webhook-description"), p, reason);

                                WebhookClient webhookClient = WebhookClient.withUrl(plugin.getBansConfig().getString("discord-webhook-url"));
                                WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();

                                embedBuilder.setTitle(new WebhookEmbed.EmbedTitle(title, null));
                                embedBuilder.setDescription(description);

                                webhookClient.send(embedBuilder.build());
                                webhookClient.close();
                            }
                        });

                    } catch (JsonProcessingException exception) {
                        System.out.println(1);
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
                        System.out.println(2);
                    }
                });
    }

    private String replaceBansPlaceholders(String raw, Player p, String reason) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", p.getName());
        placeholders.put("uuid", p.getUniqueId().toString());
        placeholders.put("reason", reason);
        placeholders.put("ip", p.getAddress().getAddress().getHostAddress());
        StrSubstitutor strSubstitutor = new StrSubstitutor(placeholders, "${", "}");

        return strSubstitutor.replace(raw);
    }
}
