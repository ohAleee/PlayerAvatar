package it.ohalee.playeravatar.listener;

import it.ohalee.playeravatar.PlayerAvatar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionHandler implements Listener {

    private final PlayerAvatar plugin;

    public ConnectionHandler(PlayerAvatar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.plugin.load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.plugin.removeFace(event.getPlayer().getUniqueId());
    }
}