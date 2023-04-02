package me.kicksquare.mcmspigot.listeners;

import me.kicksquare.mcmspigot.MCMSpigot;
import me.kicksquare.mcmspigot.SessionQueue;
import me.kicksquare.mcmspigot.types.Session;
import me.kicksquare.mcmspigot.util.ExemptUtil;
import me.kicksquare.mcmspigot.util.LoggerUtil;
import me.kicksquare.mcmspigot.util.SetupUtil;
import me.kicksquare.mcmspigot.util.UploadQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerSessionListener implements Listener {

    private final MCMSpigot plugin;

    public PlayerSessionListener(MCMSpigot plugin) {
        this.plugin = plugin;
    }

    // can only get the address from PlayerLoginEvent
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (!SetupUtil.shouldRecordSessions()) return;

        final SessionQueue sessionQueue = plugin.getSessionQueue();

        Player p = e.getPlayer();
        String address = e.getHostname().split(":")[0];

        if (ExemptUtil.isExempt(p)) return;

        Session playerSession = new Session();
        playerSession.startSessionNow(p.getUniqueId(), address);

        sessionQueue.addSession(p.getUniqueId(), playerSession);
    }

    // can only check if the player has played before in PlayerJoinEvent
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!SetupUtil.shouldRecordSessions()) return;

        final SessionQueue sessionQueue = plugin.getSessionQueue();

        Player p = e.getPlayer();

        if (ExemptUtil.isExempt(p)) return;

        Session playerSession = sessionQueue.getAndRemoveSession(p.getUniqueId());
        if (playerSession == null) {
            LoggerUtil.warning("Player joined, but could not find session in queue from PlayerLoginEvent!");
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

        if (ExemptUtil.isExempt(p)) return;

        Session playerSession = sessionQueue.getAndRemoveSession(p.getUniqueId());
        if (playerSession == null) {
            LoggerUtil.warning("Player left, but could not find session in queue from PlayerJoinEvent! This is normal after hot-reloading the plugin.");
            return;
        }
        playerSession.endSessionNow();

        CompletableFuture.runAsync(() -> {
            UploadQueue uploadQueue = plugin.getUploadQueue();

            uploadQueue.addSession(playerSession);
        });
    }
}