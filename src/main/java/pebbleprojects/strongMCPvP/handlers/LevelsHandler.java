package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pebbleprojects.strongMCPvP.databaseData.Points;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LevelsHandler {

    private final File levelsFile;
    public static LevelsHandler INSTANCE;
    private final Map<UUID, Level> prefixes;
    private final Map<Integer, Level> levels;

    public LevelsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Levels Handler...");

        INSTANCE = this;

        levels = new LinkedHashMap<>();
        prefixes = new ConcurrentHashMap<>();

        levelsFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "levels.yml");

        update();

        TaskHandler.INSTANCE.runTaskTimerAsync(new BukkitRunnable() {
            @Override
            public void run() {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    final Level level = prefixes.get(player.getUniqueId());
                    if (level != null && level.tab != null)
                        player.setPlayerListName(level.tab.replace("%player%", player.getDisplayName()));
                }
            }
        }, 1L);

        DataHandler.INSTANCE.getLogger().info("Loaded Levels Handler!");
    }

    public void update() {
        levels.clear();
        prefixes.clear();

        if (!levelsFile.exists())
            DataHandler.INSTANCE.copyToPluginDirectory("levels.yml", levelsFile);

        try {
            final Configuration levels = ConfigurationProvider.getProvider(YamlConfiguration.class).load(levelsFile);
            for (final String key : levels.getKeys()) {
                try {
                    final Level level = new Level(levels.getSection(key));
                    if (level.enabled)
                        this.levels.put(Integer.parseInt(key), level);
                } catch (final NumberFormatException ignored) {}
            }
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load levels.yml in memory: " + e.getMessage());
        }
    }

    public void updatePlayerLevel(final UUID uuid) {
        final int points = Points.INSTANCE.get(uuid);
        for (final Map.Entry<Integer, Level> entry : levels.entrySet()) {
            if (points >= entry.getKey()) {
                prefixes.put(uuid, entry.getValue());
                break;
            }
        }
    }

    public String getChatPrefix(final UUID uuid) {
        final Level level = prefixes.get(uuid);
        return level != null ? level.chat : "";
    }

    private static final class Level {

        private final boolean enabled;
        private final String tab, chat;

        private Level(final Configuration section) {
            if (section == null) {
                tab = null;
                chat = null;

                enabled = false;
                return;
            }

            tab = ChatColor.translateAlternateColorCodes('&', section.getString("tab", ""));
            chat = ChatColor.translateAlternateColorCodes('&', section.getString("chat", ""));

            enabled = !tab.isEmpty() && !chat.isEmpty();
        }
    }
}
