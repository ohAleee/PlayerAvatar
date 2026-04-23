package it.ohalee.playeravatar;

import it.ohalee.playeravatar.listener.ConnectionHandler;
import it.ohalee.playeravatar.papi.AvatarPlaceholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class PlayerAvatar extends JavaPlugin {

    private static final String AVATAR_URL = "https://mc-heads.net/avatar/%s/8.png";

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(this.executor)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Map<UUID, String> avatarCache = new ConcurrentHashMap<>();
    private String defaultAvatar;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        File defaultFile = new File(getDataFolder(), "default.png");
        if (!defaultFile.exists()) {
            saveResource("default.png", false);
        }

        this.defaultAvatar = generateAvatarFromFile(defaultFile);

        getServer().getPluginManager().registerEvents(new ConnectionHandler(this), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new AvatarPlaceholder(this).register();
        }
    }

    @Override
    public void onDisable() {
        this.executor.shutdown();
        this.avatarCache.clear();
    }

    public void load(Player player) {
        UUID uuid = player.getUniqueId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(AVATAR_URL, uuid)))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "PlayerAvatar/1.0")
                .GET()
                .build();

        this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        return this.defaultAvatar;
                    }
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(response.body())) {
                        BufferedImage img = ImageIO.read(bais);
                        return img != null ? generateAvatar(img) : this.defaultAvatar;
                    } catch (IOException e) {
                        return this.defaultAvatar;
                    }
                })
                .exceptionally(ex -> {
                    getLogger().log(Level.WARNING, "Failed to load avatar for player " + player.getName(), ex);
                    return this.defaultAvatar;
                })
                .thenAccept(serializedAvatar -> this.avatarCache.put(uuid, serializedAvatar));
    }

    public String get(Player player) {
        return this.avatarCache.getOrDefault(player.getUniqueId(), this.defaultAvatar);
    }

    public void removeFace(UUID uuid) {
        this.avatarCache.remove(uuid);
    }

    private String generateAvatarFromFile(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            return generateAvatar(image);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load default.png from data folder!", e);
            return "";
        }
    }

    private String generateAvatar(BufferedImage skinImage) {
        if (skinImage == null) return this.defaultAvatar;

        Component result = Component.empty();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char customChar = (char) ('\ua010' + j);
                int rgb = skinImage.getRGB(i, j);

                result = result.append(Component.text(customChar, TextColor.color(
                        (rgb >> 16) & 0xFF,
                        (rgb >> 8) & 0xFF,
                        rgb & 0xFF
                )));
            }
        }

        return LegacyComponentSerializer.legacySection().serialize(result);
    }
}
