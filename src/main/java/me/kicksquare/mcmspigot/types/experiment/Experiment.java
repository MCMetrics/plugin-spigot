package me.kicksquare.mcmspigot.types.experiment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.kicksquare.mcmspigot.types.experiment.enums.ExperimentTrigger;

@JsonIgnoreProperties({"server_id"})
public class Experiment {

    @JsonProperty("id") public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("conditions")
    public ExperimentCondition[] conditions;
    @JsonProperty("variants")
    public ExperimentVariant[] variants;
    @JsonProperty("trigger") public ExperimentTrigger trigger;

    @JsonProperty("consistentVariantForUuid") public boolean consistentVariantForUuid;
    @JsonProperty("variantsRandomlyOrdered") public int variantsRandomlyOrdered; // for example, if there are 3 variants, this might be 231, 312, 123, etc. The numbers are the variant indexes

    @JsonCreator
    public Experiment(@JsonProperty("name") String name, @JsonProperty("conditions") ExperimentCondition[] conditions, @JsonProperty("variants") ExperimentVariant[] variants, @JsonProperty("trigger") ExperimentTrigger trigger, @JsonProperty("id") String id, @JsonProperty("consistentVariantForUuid") boolean consistentVariantForUuid, @JsonProperty("variantsRandomlyOrdered") int variantsRandomlyOrdered) {
        this.name = name;
        this.variants = variants;
        this.conditions = conditions;
        this.trigger = trigger;
        this.id = id;

        this.consistentVariantForUuid = consistentVariantForUuid;
        this.variantsRandomlyOrdered = variantsRandomlyOrdered;
    }

    public ExperimentVariant[] getVariants() {
        return variants;
    }
    public boolean isConsistentVariantForUuid() {
        return consistentVariantForUuid;
    }
    public int getVariantsRandomlyOrdered() {
        return variantsRandomlyOrdered;
    }
}
