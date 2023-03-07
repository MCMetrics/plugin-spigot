package me.kicksquare.mcmspigot.types.experiment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentAction;

public class ExperimentVariant {

    @JsonProperty("variant")
    public int variant; // variant id as int
    @JsonProperty("name")
    public String name;
    @JsonProperty("delay")
    public int delay; // delay in seconds
    @JsonProperty("action")
    public ExperimentAction actionType;
    @JsonProperty("probability")
    public double probability;
    @JsonProperty("actionValue")
    public String actionValue;

    @JsonCreator
    public ExperimentVariant(
            @JsonProperty("variant") int variant,
            @JsonProperty("name") String name,
            @JsonProperty("delay") int delay,
            @JsonProperty("actionType") ExperimentAction actionType,
            @JsonProperty("actionValue") String actionValue,
            @JsonProperty("probability") double probability) {

        this.variant = variant;
        this.name = name;
        this.delay = delay;
        this.actionType = actionType;
        this.actionValue = actionValue;
        this.probability = probability;
    }
}