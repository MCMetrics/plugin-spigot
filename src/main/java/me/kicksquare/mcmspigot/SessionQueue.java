package me.kicksquare.mcmspigot;

import me.kicksquare.mcmspigot.types.Session;
import me.kicksquare.mcmspigot.util.LoggerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


// this tracks sessions of users who are currently online
public class SessionQueue {

    public Map<UUID, Session> sessionMap;

    private static final MCMSpigot plugin = MCMSpigot.getPlugin();

    public SessionQueue() {
        sessionMap = new HashMap<>();
    }

    public void addSession(UUID uuid, Session session) {
        sessionMap.put(uuid, session);
        LoggerUtil.debug("Added session with uuid " + uuid.toString());
    }

    public void removeSession(UUID uuid) {
        sessionMap.remove(uuid);
    }

    public Session getAndRemoveSession(UUID uuid) {
        Session session = sessionMap.get(uuid);
        removeSession(uuid);
        return session;
    }

    public void endAndUploadAllSessions() {
        for (Map.Entry<UUID, Session> entry : sessionMap.entrySet()) {
            Session session = entry.getValue();
            session.endSessionNow();

            plugin.getUploadQueue().addSession(session);
        }
    }
}
