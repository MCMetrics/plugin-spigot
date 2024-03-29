package me.kicksquare.mcmspigot.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.types.experiment.ExperimentVariant;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentTrigger;
import me.kicksquare.mcmspigot.util.ExemptUtil;
import me.kicksquare.mcmspigot.util.ExperimentUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class PapiExtension extends PlaceholderExpansion {

    private final MCMSpigot plugin;

    public PapiExtension(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "kicksquare";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mcm";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2.3";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else papi will unregister the expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        if (!player.isOnline()) {
            return null;
        }

        if (ExemptUtil.isExempt(Objects.requireNonNull(player.getPlayer()))) return "ERR_PLAYER_EXEMPT";

        ArrayList<Experiment> experiments = plugin.getExperiments();
        for (Experiment experiment : experiments) {
            if (experiment.name.equalsIgnoreCase(params)) {
                if (experiment.trigger == ExperimentTrigger.PAPI) {
                    ExperimentVariant selectedVariant = ExperimentUtil.executeActions(player.getPlayer(), experiment, -1);
                    // return the variant id that was chosen for the user
                    if (selectedVariant != null) {
                        return String.valueOf(selectedVariant.variant);
                    }
                } else {
                    return "ERR_PROVIDED_EXPERIMENT_NOT_PAPI_TRIGGERED";
                }
            }
        }
        return null;
    }
}
