package me.kicksquare.mcmspigot.types.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CampaignListItem {
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("domain")
    public String domain;

    public CampaignListItem(@JsonProperty("id") String id,
                            @JsonProperty("name") String name,
                            @JsonProperty("domain") String domain) {
        this.id = id;
        this.name = name;
        this.domain = domain;
    }
}
