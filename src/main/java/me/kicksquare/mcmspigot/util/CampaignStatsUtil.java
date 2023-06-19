package me.kicksquare.mcmspigot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.stats.CampaignResults;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

import static me.kicksquare.mcmspigot.util.ColorUtil.colorize;
import static me.kicksquare.mcmspigot.util.StringFormatterUtil.*;

public class CampaignStatsUtil {
    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    public static void handleCampaignStats(CommandSender sender, String campaignId) {
        final String bodyString = "{\"id\": \"" + campaignId + "\"}";

        sender.sendMessage(colorize("&aFetching campaign results..."));

        HttpUtil.makeAsyncPostRequest("api/campaigns/getServerCampaignResults", bodyString, HttpUtil.getAuthHeadersFromConfig()).thenAccept(response -> {
            if (response != null) {
                if (response.contains("Campaign not found")) {
                    LoggerUtil.warning("Campaign with ID " + campaignId + " not found!");
                    sender.sendMessage(colorize("&cCampaign with ID " + campaignId + " not found!"));
                    return;
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    CampaignResults results = mapper.readValue(response, CampaignResults.class);
                    sendResults(results, sender);
                } catch (JsonProcessingException exception) {
                    // if the message contains "Invalid user or server id", don't spam the console and just send one custom error
                    if (response.contains("Invalid user or server id")) {
                        LoggerUtil.severe("Error occurred while fetching campaign results: Invalid user or server id");
                        LoggerUtil.severe("Make sure your server is properly set up by running /mcmetrics setup");
                        return;
                    }
                    if (plugin.getMainConfig().getBoolean("debug")) {
                        LoggerUtil.severe("Error occurred while fetching campaign results: " + exception.getMessage());
                        exception.printStackTrace();
                    } else {
                        LoggerUtil.severe("Error occurred while fetching campaign results: " + exception.getMessage());
                        LoggerUtil.severe("Enable debug mode in config.yml for more information");
                    }
                }
            }
        });
    }

    private static void sendResults(CampaignResults results, CommandSender sender) {
        List<String> messagesFromConfig = plugin.getMainConfig().getStringList("ingame-stats");
        for (int i = 0; i < messagesFromConfig.size(); i++) {
            try {
                String message = messagesFromConfig.get(i);
                message = message.replace("%campaign_name%", results.campaignName);
                message = message.replace("%campaign_id%", results.campaignId);

                String start = formatDate(results.campaignStartDate);
                String end = formatDate(results.campaignEndDate);
                String days = String.valueOf((results.campaignEndDate.getTime() - results.campaignStartDate.getTime()) / (1000 * 60 * 60 * 24));
                if (Objects.equals(start, "N/A") || Objects.equals(end, "N/A")) {
                    days = "N/A";
                }

                String joinsPerDay = formatLargeNumber(results.totalJoins / Integer.parseInt(days));
                if (Objects.equals(days, "N/A")) {
                    joinsPerDay = "N/A";
                }

                message = message.replace("%campaign_start_date%", start);
                message = message.replace("%campaign_end_date%", end);
                message = message.replace("%days%", days);

                message = message.replace("%ad_spend%", formatMoney(results.adSpend));
                message = message.replace("%total_joins%", formatLargeNumber(results.totalJoins));
                message = message.replace("%joins_per_day%", joinsPerDay);
                message = message.replace("%total_revenue%", formatMoney(results.totalRevenue));
                message = message.replace("%roi%", formatPercentage(results.roi));
                message = message.replace("%uac%", formatMoney(results.uac));
                message = message.replace("%cac%", formatMoney(results.cac));
                message = message.replace("%arpu%", formatMoney(results.arpu));

                sender.sendMessage(colorize(message));
            } catch (Exception e) {
                LoggerUtil.severe("An error occurred while processing the message:");
                e.printStackTrace();
            }
        }
    }
}

