package me.kicksquare.mcmspigot.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.SessionQueue;
import me.kicksquare.mcmspigot.logging.Logger;
import me.kicksquare.mcmspigot.types.Session;
import me.kicksquare.mcmspigot.util.SetupUtil;
import me.kicksquare.mcmspigot.util.http.HttpUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSessionListener implements Listener {

    private MCMSpigot plugin;

    public PlayerSessionListener(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    // can only get the address from PlayerLoginEvent
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (!SetupUtil.shouldRecordSessions()) return;

        final SessionQueue sessionQueue = plugin.getSessionQueue();

        Player p = e.getPlayer();
        String address = e.getHostname().split(":")[0];

        Session playerSession = new Session();
        playerSession.startSessionNow(p.getUniqueId(), address);

        sessionQueue.addSession(p.getUniqueId(), playerSession);
    }

    // can only check if the player has played before in PlayerJoinEvent
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!SetupUtil.shouldRecordSessions()) return;

        final SessionQueue sessionQueue = plugin.getSessionQueue();

        Session playerSession = sessionQueue.getAndRemoveSession(e.getPlayer().getUniqueId());
        if (playerSession == null) {
            Logger.warning("Player joined, but could not find session in queue from PlayerLoginEvent!");
            return;
        }
        playerSession.firstSession = !e.getPlayer().hasPlayedBefore();
        sessionQueue.addSession(e.getPlayer().getUniqueId(), playerSession);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (!SetupUtil.shouldRecordSessions()) return;

        final SessionQueue sessionQueue = plugin.getSessionQueue();

        Player p = e.getPlayer();

        Session playerSession = sessionQueue.getAndRemoveSession(p.getUniqueId());
        if (playerSession == null) {
            Logger.warning("Player left, but could not find session in queue from PlayerJoinEvent! This is normal after hot-reloading the plugin.");
            return;
        }
        playerSession.endSessionNow();

        // get the session as a json string
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(playerSession);
        } catch (JsonProcessingException ex) {
            Logger.warning("Could not convert session to json string!");
            throw new RuntimeException(ex);
        }

        System.out.println("Uploading session now... " + jsonString);

        HttpUtil.makeAsyncPostRequest("http://localhost:3000/api/sessions/insertSession", jsonString, HttpUtil.getAuthHeadersFromConfig());
    }
}