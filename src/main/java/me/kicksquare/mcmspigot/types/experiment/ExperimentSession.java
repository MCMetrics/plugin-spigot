package me.kicksquare.mcmspigot.types.experiment;

public class ExperimentSession {
    public final String player_uuid;
    public final String test_id;
    public final int variant;

    public ExperimentSession(String player_uuid, String test_id, int variant) {
        this.player_uuid = player_uuid;
        this.test_id = test_id;
        this.variant = variant;
    }
}
