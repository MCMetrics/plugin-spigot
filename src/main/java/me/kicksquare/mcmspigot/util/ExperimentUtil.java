package me.kicksquare.mcmspigot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.clip.placeholderapi.PlaceholderAPI;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.types.Session;
import me.kicksquare.mcmspigot.types.experiment.Experiment;
import me.kicksquare.mcmspigot.types.experiment.ExperimentCondition;
import me.kicksquare.mcmspigot.types.experiment.ExperimentSession;
import me.kicksquare.mcmspigot.types.experiment.ExperimentVariant;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentAction;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static me.kicksquare.mcmspigot.util.ColorUtil.colorize;

public class ExperimentUtil {

    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    public static ExperimentVariant executeActions(Player p, Experiment experiment) {
        if (!plugin.getDataConfig().getBoolean("setup-complete")) return null;

        if (ExemptUtil.isExempt(p)) return null;

        // loop through conditions; if any condition is not met, return
        String bedrockPrefix = plugin.getMainConfig().getString("bedrock-prefix");
        String playerName = p.getName();
        for (ExperimentCondition condition : experiment.conditions) {
            switch (condition.type) {
                case PAPI:
                    String placeholder = condition.comparisonValue;
                    String value = condition.value; // the desired value set in the dashboard
                    String papiResult = PlaceholderAPI.setPlaceholders(p, placeholder);

                    switch (condition.comparisonType) {
                        case EQUALS:
                            if (!papiResult.equals(value)) return null;
                            break;
                        case CONTAINS:
                            if (!papiResult.contains(value)) return null;
                            break;
                        case NOT_CONTAINS:
                            if (papiResult.contains(value)) return null;
                            break;
                        case GREATER_THAN:
                            if (Integer.parseInt(placeholder) <= Integer.parseInt(value)) return null;
                            break;
                        case LESS_THAN:
                            if (Integer.parseInt(placeholder) >= Integer.parseInt(value)) return null;
                            break;
                    }
                    break;
                case BEDROCK:
                    if (!playerName.startsWith(bedrockPrefix)) return null;
                    break;
                case JAVA:
                    if (playerName.startsWith(bedrockPrefix)) return null;
                    break;
            }
        }

        ExperimentVariant[] variants = experiment.getVariants();

        ArrayList<Double> probabilitiesOfVariants = new ArrayList<>();
        for (ExperimentVariant variant : variants) {
            probabilitiesOfVariants.add(variant.probability);
        }

        int selectedVariantIndex = RandomUtil.randomWeightedElement(probabilitiesOfVariants);
        ExperimentVariant selectedVariant = variants[selectedVariantIndex];

        // if consistentVariantForUuid is true, then try to get the consistent variant (random is fallback)
        if (experiment.isConsistentVariantForUuid()) {
            double probability = RandomUtil.getProbabilityFromUuid(p.getUniqueId().toString());

            double maxProbability = 0;
            String variantsRandomlyOrdered = String.valueOf(experiment.getVariantsRandomlyOrdered());
            for (int i = 0; i < variantsRandomlyOrdered.length(); i++) {
                int variantIndex = Integer.parseInt(String.valueOf(variantsRandomlyOrdered.charAt(i))) - 1;

                maxProbability += variants[variantIndex].probability;

                if (probability <= maxProbability && probability > maxProbability - variants[variantIndex].probability) {
                    selectedVariant = variants[variantIndex];
                }
            }
        }

        // convert delay in seconds to ticks
        int delay = selectedVariant.delay * 20;


        ExperimentAction actionType = selectedVariant.actionType;
        String actionValue = selectedVariant.actionValue;
        // replace placeholders:
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", p.getName());
        placeholders.put("uuid", p.getUniqueId().toString());
        placeholders.put("variant", String.valueOf(selectedVariant.variant));
        placeholders.put("experimentName", experiment.name);
        StrSubstitutor strSubstitutor = new StrSubstitutor(placeholders, "${", "}");
        String replacedPlaceholders = strSubstitutor.replace(actionValue);

        switch (actionType) {
            case CONTROL:
                LoggerUtil.debug("Experiment: Skipping control variant!");
                break;
            case PLAYER_COMMAND:
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // make sure player is still online
                    if (!p.isOnline()) return;

                    p.performCommand(replacedPlaceholders);
                }, delay);

            case CHAT_MESSAGE:
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // make sure player is still online
                    if (!p.isOnline()) return;

                    p.sendMessage(colorize(replacedPlaceholders));
                }, delay);
                break;
            case CONSOLE_COMMAND:
                // dont execute empty commands
                if (Objects.equals(replacedPlaceholders, "") || replacedPlaceholders == null) {
                    break;
                }

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacedPlaceholders);
                }, delay);
        }

        // save data to the player's session so that it is uploaded later
        Session playerSession = plugin.getSessionQueue().getAndRemoveSession(p.getUniqueId());
        if (playerSession != null) {
            playerSession.addExperimentSession(new ExperimentSession(p.getUniqueId().toString(), experiment.id, selectedVariant.variant));
            plugin.getSessionQueue().addSession(p.getUniqueId(), playerSession);
        }

        return selectedVariant;
    }

    public static void fetchExperiments() {
        HttpUtil.makeAsyncGetRequest("api/experiments/getServerExperiments", HttpUtil.getAuthHeadersFromConfig())
                .thenAccept(response -> {
                    LoggerUtil.debug("Fetched Ab tests! Result: " + response);

                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        Experiment[] experiments = mapper.readValue(response, Experiment[].class);
                        for (Experiment experiment : experiments) {
                            // only add active experiments
                            if (experiment.active) {
                                plugin.getExperiments().add(experiment);
                            }
                        }
                        LoggerUtil.debug("Success! Number of experiments: " + plugin.getExperiments().size());
                    } catch (JsonProcessingException ex) {
                        if (response.contains("Invalid user or server id")) {
                            LoggerUtil.warning("MCMetrics: Error occurred while fetching experiments: Invalid user or server id");
                            LoggerUtil.warning("Make sure your server is properly set up by running /mcmetrics setup");
                            return;
                        }

                        if (plugin.getMainConfig().getBoolean("debug")) {
                            LoggerUtil.severe("Error occurred while fetching task list: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                        throw new RuntimeException(ex);
                    }
                });
    }
}