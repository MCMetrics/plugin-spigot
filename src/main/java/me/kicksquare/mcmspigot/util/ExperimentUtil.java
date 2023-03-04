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

    private static MCMSpigot plugin = MCMSpigot.getPlugin();

    public static ExperimentVariant executeActions(Player p, Experiment experiment) {
        if (!plugin.getConfig().getBoolean("setup-complete")) return null;

        // loop through conditions; if any condition is not met, return
        String bedrockPrefix = plugin.getConfig().getString("bedrock-prefix");
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

                System.out.println("variantIndex: " + variantIndex);
                System.out.println("probability: " + probability);
                System.out.println("maxProbability: " + maxProbability);
                System.out.println("variants length: " + variants.length);
                System.out.println("variants[variantIndex].probability: " + variants[variantIndex].probability);
                maxProbability += variants[variantIndex].probability;

                if (probability <= maxProbability && probability > maxProbability - variants[variantIndex].probability) {
                    selectedVariant = variants[variantIndex];
                }
            }
        }

        ExperimentAction actionType = selectedVariant.actionType;
        String actionValue = selectedVariant.actionValue;
        // replace placeholders:
        // ${player} -> player name
        // ${uuid} -> player uuid
        // ${variant} -> variant id
        // ${experimentName} -> experiment name
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", p.getName());
        placeholders.put("uuid", p.getUniqueId().toString());
        placeholders.put("variant", String.valueOf(selectedVariant.variant));
        placeholders.put("experimentName", experiment.name);
        StrSubstitutor strSubstitutor = new StrSubstitutor(placeholders, "${", "}");
        String replacedPlaceholders = strSubstitutor.replace(actionValue);

        switch (actionType) {
            case CONTROL:
                System.out.println("Experiment: Skipping control variant!");
                break;
            case PLAYER_COMMAND:
                p.performCommand(replacedPlaceholders);
            case CHAT_MESSAGE:
                p.sendMessage(colorize(replacedPlaceholders));
            case CONSOLE_COMMAND:
                // dont execute empty commands
                if (Objects.equals(replacedPlaceholders, "") || replacedPlaceholders == null) {
                    break;
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacedPlaceholders);
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
        HttpUtil.makeAsyncGetRequest("https://dashboard.mcmetrics.net/api/experiments/getServerExperiments", HttpUtil.getAuthHeadersFromConfig())
                .thenAccept(result -> {
                    System.out.println("----- Fetched Ab tests! Result: " + result);

                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        Experiment[] experiments = mapper.readValue(result, Experiment[].class);
                        for (Experiment experiment : experiments) {
                            plugin.getExperiments().add(experiment);
                        }
                        System.out.println("Success! Number of ab tests: " + plugin.getExperiments().size());
                    } catch (JsonProcessingException ex) {
                        System.out.println("Error occurred while fetching ab tests:");
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                });
    }
}