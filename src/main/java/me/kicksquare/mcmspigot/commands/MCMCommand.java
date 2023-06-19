package me.kicksquare.mcmspigot.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.SessionQueue;
import me.kicksquare.mcmspigot.types.Session;
import me.kicksquare.mcmspigot.types.TaskList;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.types.experiment.ExperimentCondition;
import me.kicksquare.mcmspigot.types.experiment.ExperimentVariant;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentAction;
import me.kicksquare.mcmspigot.types.stats.CampaignListItem;
import me.kicksquare.mcmspigot.util.ConfigUtil;
import me.kicksquare.mcmspigot.util.ExperimentUtil;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.SetupUtil;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static me.kicksquare.mcmspigot.util.CampaignStatsUtil.handleCampaignStats;
import static me.kicksquare.mcmspigot.util.ClickableMessageUtil.sendClickableCommand;
import static me.kicksquare.mcmspigot.util.ColorUtil.colorize;

@SuppressWarnings("SameReturnValue")
public class MCMCommand implements CommandExecutor {

    private static final MCMSpigot staticPlugin = MCMSpigot.getPlugin();
    private final MCMSpigot plugin;

    public MCMCommand(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    public static CompletableFuture<Boolean> reloadConfigAndFetchTasks() {
        return CompletableFuture.supplyAsync(() -> {
            LoggerUtil.debug("Reloading config...");

            ConfigUtil.setConfigDefaults(staticPlugin.getMainConfig(), staticPlugin.getDataConfig(), staticPlugin.getBansConfig());

            staticPlugin.getMainConfig().forceReload();
            staticPlugin.getDataConfig().forceReload();
            staticPlugin.getBansConfig().forceReload();

            if (SetupUtil.isSetup()) {
                LoggerUtil.debug("Server is set up! Fetching experiments and tasks...");

                staticPlugin.getExperiments().clear();
                ExperimentUtil.fetchExperiments();
                TaskList.fetchTasks();
            } else {
                LoggerUtil.warning("Reloaded plugin, but the plugin is not configured! Please run /mcmetrics setup <user id> <server id> to configure the plugin.");
            }

            return true;
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        if (label.equals("/mcmetrics")) {
            if (sender instanceof ConsoleCommandSender && args.length == 3 && args[0].equals("setup")) {
                return setup(sender, args);
            } else {
                return false;
            }
        }

        if (sender instanceof Player && !sender.hasPermission("mcmetrics.command")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(colorize("&e&lMCMetrics &r&7Reloading config..."));
            reloadConfigAndFetchTasks().thenAccept((result) -> {
                plugin.uploadPlayerCount(); // manually force upload player count
                if (result) {
                    sender.sendMessage(colorize("&a&lMCMetrics &r&7Reloaded successfully!"));
                } else {
                    sender.sendMessage(colorize("&c&lMCMetrics &r&7Reload failed! Please check the console for more information."));
                }
            });

            return true;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("setup")) {
            return setup(sender, args);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("uploadall")) {
            sender.sendMessage(colorize("&e&lMCMetrics &r&7Uploading all sessions in the upload queue..."));
            plugin.getUploadQueue().uploadAll();
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("experiments")) {
            listExperiments(sender);
            return true;
        } else if (args.length == 4 && args[0].equalsIgnoreCase("testexperiment")) {
            return testExperiment(sender, args);
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("bans")) {
            if (!plugin.getBansConfig().getBoolean("enabled")) {
                sender.sendMessage(colorize("&c&lMCMetrics &r&7Global Bans is not enabled!"));
                return true;
            }

            // returns false if the help message needs to be shown
            if (BansExecutor.executeBansSubcommand(sender, args)) return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("campaigns")) {
            printCampaigns(sender);
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("campaignstats")) {
            String campaignId = args[1];
            handleCampaignStats(sender, campaignId);
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("playerinfo")) {
            String playerName = args[1];
            sendPlayerInfo(sender, playerName);
            return true;
        }

        sender.sendMessage(colorize("&e&lMCMetrics" + " &7Version: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(colorize("&7Currently tracking &e&l" + plugin.getSessionQueue().getQueueSize() + " &7sessions."));
        sender.sendMessage(colorize("&e&l" + plugin.getUploadQueue().getSize() + " &7sessions queued for upload."));
        sender.sendMessage(colorize("&7Plugin Commands:"));
        sender.sendMessage(colorize("&7 • &b/mcmetrics reload &7- Reloads the config"));
        sender.sendMessage(colorize("&7 • &b/mcmetrics experiments &7- Lists all active experiments"));
        sender.sendMessage(colorize("&7 • &b/mcmetrics campaigns &7- Lists campaigns. Click for statistics."));
        sender.sendMessage(colorize("&7 • &b/mcmetrics playerinfo <player name> &7- Shows information about an online player."));
        sender.sendMessage(colorize("&7 • &b/mcmetrics setup <user id> <server id> &7- Automatically configures the plugin"));
        sender.sendMessage(colorize("&7 • &b/mcmetrics uploadall &7- Manually uploads all sessions in the upload queue - intended for testing."));
        sender.sendMessage(colorize("&7 • &b/mcmetrics testexperiment <player name> <experiment name> <variant> &7- Manually triggers an experiment with a set variant. Intended for testing."));
        sender.sendMessage(colorize("&7 • &b/mcmexperiment <player name> <experiment name> &7- Manually triggers an experiment. Console only."));
        sender.sendMessage(colorize("&7 • &b/mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id> &7- Manually triggers a payment. Console only."));
        if (plugin.getBansConfig().getBoolean("enabled")) {
            sender.sendMessage(colorize("&7Global Bans Commands:"));
            sender.sendMessage(colorize("&7 • &b/mcmetrics bans add <player name/uuid> <reason> <evidence> &7- Bans a player using MCMetrics Global Bans"));
            sender.sendMessage(colorize("&7 • &b/mcmetrics bans lookup <player name/uuid> &7- Check a player for MCMetrics Global Bans flags"));
        }

        return true;
    }

    private void sendPlayerInfo(CommandSender sender, String playerName) {
        // try to find the Player
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(colorize("&e&lMCMetrics &r&7Could not find player &f" + playerName + "&7."));
            return;
        }

        // try to find the player in the session queue
        SessionQueue sessionQueue = plugin.getSessionQueue();
        Session session = sessionQueue.sessionMap.get(player.getUniqueId());
        if (session == null) {
            sender.sendMessage(colorize("&e&lMCMetrics &r&7Could not find player &f" + playerName + "&7 in the session queue."));
            return;
        }

        String domain = session.domain;
        boolean firstSession = session.firstSession;
        // parses the date from the date string
        String dateString = session.join_time;
        Date joinTime;
        try {
            joinTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
        } catch (ParseException e) {
            sender.sendMessage(colorize("&e&lMCMetrics &r&7Could not parse date string &f" + dateString + "&7."));
            return;
        }
        // calculate the session duration
        long sessionDuration = System.currentTimeMillis() - joinTime.getTime();

        // convert sessionDuration to hours, minutes, and seconds
        long hours = TimeUnit.MILLISECONDS.toHours(sessionDuration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(sessionDuration) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(sessionDuration) % 60;

        // format the sessionDurationString
        String sessionDurationString = String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);

        sender.sendMessage(colorize("&e&lMCMetrics &r&7Player &f" + playerName + "&7 is currently online."));
        sender.sendMessage(colorize("&7 • &7Join Domain: &f" + domain));
        sender.sendMessage(colorize("&7 • &7First Session: &f" + firstSession));
        sender.sendMessage(colorize("&7 • &7Current Session Duration: &f" + sessionDurationString));
    }

    private boolean testExperiment(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage(colorize("&cUsage: &b/mcmetrics testexperiment <player name> <experiment name> <variant>"));
            return true;
        }

        String playerName = args[1];
        String experimentName = args[2];
        String variant = args[3];

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(colorize("&cPlayer not found!"));
            return true;
        }

        ArrayList<Experiment> experiments = plugin.getExperiments();
        Experiment experiment = null;
        for (Experiment e : experiments) {
            if (e.name.equalsIgnoreCase(experimentName)) {
                experiment = e;
                break;
            } else if (e.name.replaceAll(" ", "_").equalsIgnoreCase(experimentName)) {
                // experiments with spaces in the name are replaced with underscores in this test command
                experiment = e;
                break;
            }
        }

        if (experiment == null) {
            sender.sendMessage(colorize("&cExperiment not found!"));
            return true;
        }

        ExperimentVariant targetVariant = null;
        for (ExperimentVariant v : experiment.variants) {
            if (v.variant == Integer.parseInt(variant)) {
                targetVariant = v;
                break;
            }
        }

        if (targetVariant == null) {
            sender.sendMessage(colorize("&cVariant not found!"));
            return true;
        }

        ExperimentVariant selectedVariant = ExperimentUtil.executeActions(targetPlayer, experiment, targetVariant.variant);

        if (selectedVariant == null) {
            sender.sendMessage(colorize("&cFailed to execute experiment! Please check the console for more information."));
        } else {
            sender.sendMessage(colorize("&aExperiment executed successfully!"));
        }
        return true;
    }

    private boolean setup(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(colorize("&cUsage: &f/mcmetrics setup <user id> <server id>"));
            sender.sendMessage("Get this command from the dashboard!");
            return true;
        }

        final String uid = args[1];
        final String server_id = args[2];

        // mcmetrics setup <uid> <server_id>
        if (uid.length() != 25) {
            sender.sendMessage("Invalid UID. UID must be 25 characters long.");
            return true;
        } else if (server_id.length() != 36) {
            sender.sendMessage("Invalid server ID. Server ID must be 36 characters long.");
            return true;
        } else {
            plugin.getMainConfig().set("uid", uid);
            plugin.getMainConfig().set("server_id", server_id);
            plugin.getDataConfig().set("setup-complete", true);
            plugin.getMainConfig().forceReload();
            plugin.getDataConfig().forceReload();
            plugin.getBansConfig().forceReload();

            CompletableFuture.supplyAsync(() -> {
                LoggerUtil.debug("Setting server as setup...");
                HttpUtil.makeAsyncGetRequest("api/server/setServerIsSetup", HttpUtil.getAuthHeadersFromConfig());
                LoggerUtil.debug("Fetching experiments...");
                reloadConfigAndFetchTasks();
                return true;
            }).thenAccept((result) -> {
                sender.sendMessage("Server configured successfully!");
            });

            return true;
        }
    }

    public void listExperiments(CommandSender sender) {
        ArrayList<Experiment> experiments = plugin.getExperiments();
        if (experiments.size() == 0) {
            sender.sendMessage(colorize("&cNo experiments found."));
        } else {
            sender.sendMessage(colorize("&7Found &e&l" + experiments.size() + "&7 active experiments:"));
            for (Experiment experiment : experiments) {
                if (experiment.variants == null) {
                    sender.sendMessage(colorize("&7 • &a" + experiment.name + "&8&o (no variants)"));
                } else {
                    sender.sendMessage(colorize("&7 • &7'&a" + experiment.name + "&7'&8&o (" + experiment.variants.length + " variants)"));

                    if (experiment.conditions != null && experiment.conditions.length > 0) {
                        sender.sendMessage(colorize("&7   • " + "Conditions:"));
                        for (ExperimentCondition condition : experiment.conditions) {
                            switch (condition.type) {
                                case PAPI:
                                    String comparisonType = "equals";
                                    switch (condition.comparisonType) {
                                        case EQUALS:
                                            comparisonType = "equals";
                                            break;
                                        case CONTAINS:
                                            comparisonType = "contains";
                                            break;
                                        case GREATER_THAN:
                                            comparisonType = "greater than";
                                            break;
                                        case LESS_THAN:
                                            comparisonType = "less than";
                                            break;
                                        case NOT_CONTAINS:
                                            comparisonType = "does not contain";
                                            break;

                                    }

                                    sender.sendMessage(colorize("&7     • &7Placeholder &a" + condition.comparisonValue + " &7" + comparisonType + " &a" + condition.value));
                                    break;
                                case JAVA:
                                    sender.sendMessage(colorize("&7     • &7Only " + "&aJava " + "&7players"));
                                    break;
                                case BEDROCK:
                                    sender.sendMessage(colorize("&7     • &7Only " + "&aBedrock " + "&7players"));
                                    break;
                            }
                        }
                    }


                    sender.sendMessage(colorize("&7   • " + "Variants:"));
                    for (ExperimentVariant variant : experiment.variants) {
                        if (variant.actionType == ExperimentAction.CONTROL) {
                            sender.sendMessage(colorize("&7     • &7'&a" + variant.name + "&7' &8&o(" + (variant.probability * 100) + "%) &9&lControl Variant"));
                        } else {
                            String actionTypeString = "Unknown";

                            switch (variant.actionType) {
                                case CONSOLE_COMMAND:
                                    actionTypeString = "Console Command";
                                    break;
                                case PLAYER_COMMAND:
                                    actionTypeString = "Player Command";
                                    break;
                                case CHAT_MESSAGE:
                                    actionTypeString = "Chat Message";
                                    break;
                            }

                            sender.sendMessage(colorize("&7     • &7'&a" + variant.name + "&7' &8&o(" + (variant.probability * 100) + "%)&7: &9&l" + actionTypeString + "&7: &a" + variant.actionValue));
                        }
                    }
                }
            }
        }
    }

    private void printCampaigns(CommandSender sender) {
        sender.sendMessage(colorize("&7Fetching campaigns..."));

        HttpUtil.makeAsyncGetRequest("api/campaigns/getServerCampaigns", HttpUtil.getAuthHeadersFromConfig()).thenAccept(response -> {
            if (response != null) {
                if (response.contains("NO_ACCESS")) {
                    sender.sendMessage(colorize("&cOnly projects on the Growth plan can use campaigns."));
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    CampaignListItem[] results = mapper.readValue(response, CampaignListItem[].class);

                    if (results.length == 0) {
                        sender.sendMessage(colorize("&cNo campaigns found."));
                    } else {
                        sender.sendMessage(colorize("&7Found &e&l" + results.length + "&7 active campaigns:"));
                        for (CampaignListItem result : results) {
                            if (sender instanceof Player) {
                                sendClickableCommand((Player) sender, "&7 • &a" + result.name + "&8&o (" + result.domain + ")", "mcmetrics campaignstats " + result.id);
                            } else {
                                sender.sendMessage(colorize("&7 • &a" + result.name + "&8&o (" + result.domain + "). Run &a/mcmetrics campaignstats " + result.id + "&8&o for more info."));
                            }
                        }
                    }
                } catch (JsonProcessingException exception) {
                    // if the message contains "Invalid user or server id", don't spam the console and just send one custom error
                    if (response.contains("Invalid user or server id")) {
                        LoggerUtil.severe("Error occurred while fetching campaigns: Invalid user or server id");
                        LoggerUtil.severe("Make sure your server is properly set up by running /mcmetrics setup");
                        return;
                    }
                    if (plugin.getMainConfig().getBoolean("debug")) {
                        LoggerUtil.severe("Error occurred while fetching campaigns: " + exception.getMessage());
                        exception.printStackTrace();
                    } else {
                        LoggerUtil.severe("Error occurred while fetching campaigns: " + exception.getMessage());
                        LoggerUtil.severe("Enable debug mode in config.yml for more information");
                    }
                }
            }
        });
    }
}