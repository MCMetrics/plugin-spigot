package me.kicksquare.mcmspigot.commands;

import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.TaskList;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.types.experiment.ExperimentCondition;
import me.kicksquare.mcmspigot.types.experiment.ExperimentVariant;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentAction;
import me.kicksquare.mcmspigot.util.ExperimentUtil;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.SetupUtil;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static me.kicksquare.mcmspigot.util.ClickableMessageUtil.sendClickableCommand;
import static me.kicksquare.mcmspigot.util.ColorUtil.colorize;

public class MCMCommand implements CommandExecutor {

    private final MCMSpigot plugin;
    private static final MCMSpigot staticPlugin = MCMSpigot.getPlugin();

    public MCMCommand(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
            reloadConfigAndFetchData().thenAccept((result) -> {
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
        } else if (args.length == 1 && args[0].equalsIgnoreCase("enablesentry")) {
            sender.sendMessage("Enabling Sentry...");
            plugin.getMainConfig().set("enable-sentry", true);
            plugin.getMainConfig().forceReload();
            sender.sendMessage(colorize("&a&lSentry Enabled! &r&7Thank you for helping us improve the plugin!"));
            return true;
        }

        sender.sendMessage(colorize("&e&lMCMetrics" + " &7Version: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(colorize("&7Currently tracking &e&l" + plugin.getUploadQueue().getSize() + " &7sessions in the upload queue"));
        sender.sendMessage(colorize("&7Plugin Commands:"));
        sender.sendMessage(colorize("&7 • &b/mcmetrics reload &7- Reloads the config"));
        sender.sendMessage(colorize("&7 • &b/mcmetrics experiments &7- Lists all active experiments"));
        sender.sendMessage(colorize("&7 • &b/mcmetrics setup <user id> <server id> &7- Automatically configures the plugin"));
        sender.sendMessage(colorize("&7 • &b/mcmetrics uploadall &7- Manually uploads all sessions in the upload queue - intended for testing."));
        sender.sendMessage(colorize("&7 • &b/mcmexperiment <player name> <experiment name> &7- Manually triggers an experiment. Console only."));
        sender.sendMessage(colorize("&7 • &b/mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id> &7- Manually triggers a payment. Console only."));

        return true;
    }

    public static CompletableFuture<Boolean> reloadConfigAndFetchData() {
        return CompletableFuture.supplyAsync(() -> {
            LoggerUtil.debug("Reloading config...");

            staticPlugin.getMainConfig().forceReload();
            staticPlugin.getDataConfig().forceReload();

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

    public boolean setup(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("Usage: /mcmetrics setup <user id> <server id>");
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

            CompletableFuture.supplyAsync(() -> {
                LoggerUtil.debug("Setting server as setup...");
                HttpUtil.makeAsyncGetRequest("api/server/setServerIsSetup", HttpUtil.getAuthHeadersFromConfig());
                LoggerUtil.debug("Fetching experiments...");
                reloadConfigAndFetchData();
                return true;
            }).thenAccept((result) -> {
                sender.sendMessage("Server configured successfully!");
                if (sender instanceof Player) {
                    sendClickableCommand((Player) sender, "&e&lOptional Sentry Opt-In: &r&7Click here to enable anonymous error-reporting via Sentry (you can change this later in the config).", "mcmetrics enablesentry");
                } else {
                    sender.sendMessage("Optional Sentry Opt-In: Run 'mcmetrics enablesentry' to enable anonymous error-reporting via Sentry (you can change this later in the config).");
                }
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
}