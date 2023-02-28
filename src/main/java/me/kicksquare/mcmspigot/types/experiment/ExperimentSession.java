package me.kicksquare.mcmspigot.types.experiment;

public class ExperimentSession {
    public String player_uuid;
    public String test_id;
    public int variant;

    public ExperimentSession(String player_uuid, String test_id, int variant) {
        this.player_uuid = player_uuid;
        this.test_id = test_id;
        this.variant = variant;
    }
}
