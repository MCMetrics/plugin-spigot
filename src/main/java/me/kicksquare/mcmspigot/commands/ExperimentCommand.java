package me.kicksquare.mcmspigot.commands;

import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentTrigger;
import me.kicksquare.mcmspigot.util.ExemptUtil;
import me.kicksquare.mcmspigot.util.ExperimentUtil;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.SetupUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ExperimentCommand implements CommandExecutor {

    private final MCMSpigot plugin;

    public ExperimentCommand(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage("This command can only be run from the console.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /mcmexperiment <player name> <experiment name>");
            return true;
        }

        if (!SetupUtil.shouldExecuteExperiments()) {
            sender.sendMessage("Experiments are disabled.");
            return true;
        }

        final String playerName = args[0];
        final Player player = plugin.getServer().getPlayer(playerName);

        if (ExemptUtil.isExempt(player)) {
            sender.sendMessage("Player is exempt from experiments! You can update this in config.yml.");
            return true;
        }

        // args 2 and later are the experiment name
        final String experimentName = String.join(" ", args).substring(args[0].length() + 1);

        if (player == null) {
            sender.sendMessage("Player not found.");
            return true;
        }

        ArrayList<Experiment> experiments = plugin.getExperiments();
        for (Experiment experiment : experiments) {
            LoggerUtil.debug("checking experiment " + experiment.name + " with trigger " + experiment.trigger);
            if (experiment.name.equalsIgnoreCase(experimentName) && experiment.trigger == ExperimentTrigger.COMMAND) {
                LoggerUtil.debug("Found experiment!");
                ExperimentUtil.executeActions(player, experiment);
                sender.sendMessage("Experiment executed successfully!");
                return true;
            }
        }
        sender.sendMessage("Experiment not found.");
        return true;
    }
}
