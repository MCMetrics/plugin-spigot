package me.kicksquare.mcmspigot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.types.Session;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.http.HttpUtil;

import java.util.ArrayList;

// this tracks sessions that are already finished (user logged off) to be uploaded in bulk later on
@SuppressWarnings("CanBeFinal")
public class UploadQueue {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final MCMSpigot plugin;

    public ArrayList<Session> sessions;

    public UploadQueue(MCMSpigot plugin) {
        this.plugin = plugin;
        sessions = new ArrayList<>();
    }

    public void uploadAll() {
        // get the session as a json string
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(sessions);
        } catch (JsonProcessingException ex) {
            LoggerUtil.severe("Could not convert sessions to json string!");
            throw new RuntimeException(ex);
        }

        LoggerUtil.debug("Uploading session now... " + jsonString);

        HttpUtil.makeAsyncPostRequest("api/sessions/insertBulkSessions", jsonString, HttpUtil.getAuthHeadersFromConfig());
        clear();
    }

    public void addSession(Session session) {
        sessions.add(session);

        if (sessions.size() >= plugin.getDataConfig().getInt("bulk-session-threshold")) {
            uploadAll();
        }
    }

    public void clear() {
        sessions.clear();
    }

    public int getSize() {
        return sessions.size();
    }
}
