package me.kicksquare.mcmspigot.types.experiment;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.kicksquare.mcmspigot.types.experiment.enums.ConditionComparisonType;
import me.kicksquare.mcmspigot.types.experiment.enums.ConditionType;

public class ExperimentCondition {
    @JsonProperty("id")
    public int id;
    @JsonProperty("type")
    public ConditionType type;
    @JsonProperty("comparisonType")
    public ConditionComparisonType comparisonType;
    @JsonProperty("comparisonValue")
    public String comparisonValue;
    @JsonProperty("value")
    public String value;

    public ExperimentCondition(@JsonProperty("id") int id, @JsonProperty("type") ConditionType type, @JsonProperty("comparisonType") ConditionComparisonType comparisonType, @JsonProperty("comparisonValue") String comparisonValue, @JsonProperty("value") String value) {
        this.id = id;
        this.type = type;
        this.comparisonType = comparisonType;
        this.comparisonValue = comparisonValue;
        this.value = value;
    }
}
