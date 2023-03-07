package me.kicksquare.mcmspigot.listeners;

import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentTrigger;
import me.kicksquare.mcmspigot.util.ExperimentUtil;
import me.kicksquare.mcmspigot.util.SetupUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;

public class ExperimentListener implements Listener {

    private final MCMSpigot plugin;

    public ExperimentListener(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!SetupUtil.shouldExecuteExperiments()) return;

        Player p = e.getPlayer();

        ArrayList<Experiment> experiments = plugin.getExperiments();
        for (Experiment experiment : experiments) {
            if (experiment.trigger == ExperimentTrigger.JOIN || (experiment.trigger == ExperimentTrigger.FIRST_JOIN && !p.hasPlayedBefore())) {
                ExperimentUtil.executeActions(p, experiment);
            }
        }
    }
}