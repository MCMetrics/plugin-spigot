package me.kicksquare.mcmspigot.types.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Date;

public class CampaignResults {
    @JsonProperty("adSpend")
    public Float adSpend;
    @JsonProperty("totalJoins")
    public Integer totalJoins;
    @JsonProperty("totalRevenue")
    public Float totalRevenue;
    @JsonProperty("roi")
    public Float roi;
    @JsonProperty("uac")
    public Float uac;
    @JsonProperty("cac")
    public Float cac;
    @JsonProperty("arpu")
    public Float arpu;

    @JsonProperty("campaignName")
    public String campaignName;
    @JsonProperty("campaignId")
    public String campaignId;
    @JsonProperty("campaignStartDate")
    public Date campaignStartDate;
    @JsonProperty("campaignEndDate")
    public Date campaignEndDate;

    public CampaignResults(@JsonProperty("adSpend") float adSpend,
                           @JsonProperty("totalJoins") int totalJoins,
                           @JsonProperty("totalRevenue") float totalRevenue,
                           @JsonProperty("roi") float roi,
                           @JsonProperty("uac") float uac,
                           @JsonProperty("cac") float cac,
                           @JsonProperty("arpu") float arpu,

                           @JsonProperty("campaignName") String campaignName,
                           @JsonProperty("campaignId") String campaignId,
                           @JsonProperty("campaignStartDate") String campaignStartDate,
                           @JsonProperty("campaignEndDate") String campaignEndDate) {
        this.adSpend = adSpend;
        this.totalJoins = totalJoins;
        this.totalRevenue = totalRevenue;
        this.roi = roi;
        this.uac = uac;
        this.cac = cac;
        this.arpu = arpu;

        this.campaignName = campaignName;
        this.campaignId = campaignId;
        this.campaignStartDate = Date.from(Instant.parse(campaignStartDate));
        this.campaignEndDate = Date.from(Instant.parse(campaignEndDate));
    }
}
