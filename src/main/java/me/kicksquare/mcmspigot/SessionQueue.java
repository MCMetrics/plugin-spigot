package me.kicksquare.mcmspigot;

import me.kicksquare.mcmspigot.types.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static me.kicksquare.mcmspigot.listeners.PlayerSessionListener.uploadSession;

public class SessionQueue {

    public Map<UUID, Session> sessionMap;

    public SessionQueue() {
        sessionMap = new HashMap<>();
    }

    public void addSession(UUID uuid, Session session) {
        sessionMap.put(uuid, session);
        System.out.println("Added session with uuid " + uuid.toString());
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

            CompletableFuture.runAsync(() -> {
                uploadSession(session);
            });
        }
    }
}
