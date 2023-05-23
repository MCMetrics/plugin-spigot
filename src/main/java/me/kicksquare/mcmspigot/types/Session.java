package me.kicksquare.mcmspigot.types;

import me.kicksquare.mcmspigot.types.experiment.ExperimentSession;
import me.kicksquare.mcmspigot.util.LoggerUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class Session {
    public String player_uuid;
    public String join_time;
    public String leave_time;
    public String domain;
    public boolean firstSession = false;

    public final ArrayList<ExperimentSession> experimentSessions = new ArrayList<>(); // data about any ab tests during this session

    public void startSessionNow(UUID uuid, String domain) {
        this.player_uuid = uuid.toString();

        // set join time to current time
        this.join_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        this.domain = domain;
    }

    public void endSessionNow() {
        if (this.player_uuid == null || this.join_time == null || this.domain == null) {
            LoggerUtil.severe("Session is missing required fields!");
        }

        // set leave to current time
        this.leave_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public void addExperimentSession(ExperimentSession experimentSession) {
        experimentSessions.add(experimentSession);
    }
}
