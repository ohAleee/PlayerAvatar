package it.ohalee.playeravatar.papi;

import it.ohalee.playeravatar.PlayerAvatar;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class AvatarPlaceholder extends PlaceholderExpansion {

    private final PlayerAvatar plugin;

    public AvatarPlaceholder(PlayerAvatar plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "avatar";
    }

    @Override
    public String getAuthor() {
        return "ohAlee";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || player.getPlayer() == null || !params.equalsIgnoreCase("face")) return null;
        return this.plugin.get(player.getPlayer());
    }

}
