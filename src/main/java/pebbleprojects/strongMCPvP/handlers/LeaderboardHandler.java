package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;
import pebbleprojects.strongMCPvP.databaseData.*;
import pebbleprojects.strongMCPvP.functions.TopPlayerData;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;
import pebbleprojects.strongMCPvP.handlers.papi.PlaceholderAPIHandler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LeaderboardHandler {

    private Configuration leaderboards;
    private final File leaderboardsFile;
    public static LeaderboardHandler INSTANCE;
    private final Map<String, Leaderboard> leaderboardsList;

    public LeaderboardHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Leaderboard Handler...");

        INSTANCE = this;

        leaderboardsList = new ConcurrentHashMap<>();
        leaderboardsFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "leaderboards.yml");

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Leaderboard Handler...");
    }

    public void update() {
        if (!leaderboardsFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("leaderboards.yml", leaderboardsFile);
        }

        try {
            leaderboards = ConfigurationProvider.getProvider(YamlConfiguration.class).load(leaderboardsFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load leaderboards.yml in memory: " + e.getMessage());
        }

        updateLeaderboards();
    }

    public void shutdown() {
        for (final Leaderboard leaderboard : leaderboardsList.values())
            leaderboard.destroy();
    }

    public void createLeaderboard(final Location location, final String type) throws SQLException {
        DataHandler.INSTANCE.getData().set("leaderboards." + type, LocationHandler.INSTANCE.convertToString(location));
        DataHandler.INSTANCE.saveData();

        setupLeaderboard(type);
    }

    public void removeLeaderboard(final String type) throws SQLException {
        DataHandler.INSTANCE.getData().set("leaderboards." + type, null);
        DataHandler.INSTANCE.saveData();

        setupLeaderboard(type);
    }

    private void updateLeaderboards() {
        for (final Leaderboard leaderboard : leaderboardsList.values())
            leaderboard.destroy();

        try {
            setupLeaderboard("kills");
            setupLeaderboard("souls");
            setupLeaderboard("points");
            setupLeaderboard("deaths");
            setupLeaderboard("assists");
        } catch (final SQLException e) {
            DataHandler.INSTANCE.getLogger().severe("Error while fetching top players: " + e.getMessage());
        }
    }

    private void setupLeaderboard(final String key) throws SQLException {
        if (leaderboardsList.containsKey(key))
            leaderboardsList.remove(key).destroy();

        final Configuration section = leaderboards.getSection(key);
        if (section == null || !section.getBoolean("enabled", false)) return;

        final Location location = LocationHandler.INSTANCE.convertToLocation(DataHandler.INSTANCE.getData().getString("leaderboards." + key));
        if (location == null) return;

        final List<Map.Entry<TopPlayerData, Integer>> leaderboard;
        switch (key) {
            case "kills":
                leaderboard = Kills.INSTANCE.getTop10Players();
                break;
            case "deaths":
                leaderboard = Deaths.INSTANCE.getTop10Players();
                break;
            case "assists":
                leaderboard = Assists.INSTANCE.getTop10Players();
                break;
            case "souls":
                leaderboard = Souls.INSTANCE.getTop10Players();
                break;
            case "points":
                leaderboard = Points.INSTANCE.getTop10Players();
                break;
            default:
                leaderboard = null;
                break;
        }

        createLeaderboard(key, location, leaderboard, ChatColor.translateAlternateColorCodes('&', section.getString("title", "")), ChatColor.translateAlternateColorCodes('&', section.getString("lines-format", "")));
    }

    private void createLeaderboard(final String key, final Location location, final List<Map.Entry<TopPlayerData, Integer>> leaderboard, final String title, final String lineFormat) {
        if (leaderboard == null || leaderboard.isEmpty())
            throw new IllegalArgumentException("Leaderboard data cannot be null or empty!");

        final Location currentLocation = location.clone().add(0, 3, 0);
        if (leaderboardsList.containsKey(key))
            leaderboardsList.remove(key).destroy();

        final Leaderboard leaderboardInstance = new Leaderboard();

        TaskHandler.INSTANCE.runSync(() -> {
            leaderboardInstance.stands.add(spawnArmorStand(currentLocation, title));
            currentLocation.subtract(0, 0.3, 0);

            int rank = 1;
            for (final Map.Entry<TopPlayerData, Integer> entry : leaderboard) {
                leaderboardInstance.stands.add(spawnArmorStand(currentLocation, PlaceholderAPIHandler.INSTANCE.translateMessage(entry.getKey().getUUID(),
                        lineFormat.replace("%place%", String.valueOf(rank++))
                                .replace("%player%", entry.getKey().getName())
                                .replace("%score%", String.valueOf(entry.getValue())))));

                currentLocation.subtract(0, 0.3, 0);
            }

            leaderboardsList.put(key, leaderboardInstance);
        });
    }

    private ArmorStand spawnArmorStand(final Location location, final String name) {
        final ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        armorStand.setCustomName(name);
        armorStand.setCustomNameVisible(true);
        armorStand.setVisible(false);
        armorStand.setMarker(true);
        armorStand.setGravity(false);

        return armorStand;
    }

    private static final class Leaderboard {

        private BukkitTask task;
        private final List<ArmorStand> stands;

        private Leaderboard() {
            stands = new ArrayList<>();
        }

        private void destroy() {
            if (task != null) {
                task.cancel();
                task = null;
            }

            for (final ArmorStand stand : stands)
                stand.remove();

            stands.clear();
        }
    }

}
