package me.kicksquare.mcmspigot.types.bans;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class GlobalBansResponseEntry {
    @JsonProperty("player_uuid")
    public String player_uuid;
    @JsonProperty("ban_reason")
    public BanReason ban_reason;
    @JsonProperty("id")
    public String id;
    @JsonProperty("evidence")
    public String evidence;
    @JsonProperty("global_bans_identifier")
    public String global_bans_identifier;
    @JsonProperty("ban_timestamp")
    public Date ban_time;


    public GlobalBansResponseEntry(@JsonProperty("player_uuid") String player_uuid,
                                   @JsonProperty("ban_reason") BanReason ban_reason,
                                   @JsonProperty("id") String id,
                                   @JsonProperty("evidence") String evidence,
                                   @JsonProperty("global_bans_identifier") String global_bans_identifier,
                                   @JsonProperty("ban_timestamp") long ban_timestamp) {
        this.player_uuid = player_uuid;
        this.ban_reason = ban_reason;
        this.id = id;
        this.evidence = evidence;
        this.global_bans_identifier = global_bans_identifier;
        this.ban_time = new Date(ban_timestamp);
    }
}
