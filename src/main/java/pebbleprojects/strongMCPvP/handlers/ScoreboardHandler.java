package pebbleprojects.strongMCPvP.handlers;

import dev.mqzen.boards.BoardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.functions.ScoreboardAdapter;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ScoreboardHandler {

    private String title;
    private List<String> lines;
    private Configuration scoreboard;
    private final File scoreboardFile;
    public static ScoreboardHandler INSTANCE;

    public ScoreboardHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Scoreboard Handler...");

        INSTANCE = this;

        scoreboardFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "scoreboard.yml");

        BoardManager.load(DataHandler.INSTANCE.getMain());

        update();
        BoardManager.getInstance().startBoardUpdaters();

        DataHandler.INSTANCE.getLogger().info("Loaded Scoreboard Handler!");
    }

    public void update() {
        if (!scoreboardFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("scoreboard.yml", scoreboardFile);
        }

        try {
            scoreboard = ConfigurationProvider.getProvider(YamlConfiguration.class).load(scoreboardFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load scoreboard.yml in memory: " + e.getMessage());
        }

        title = ChatColor.translateAlternateColorCodes('&', scoreboard.getString("title", "&6PvP"));
        lines = scoreboard.getStringList("lines");

        lines.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));

        BoardManager.getInstance().setUpdateInterval(scoreboard.getLong("update-interval", 4L));

        for (final Player player : Bukkit.getOnlinePlayers()) {
            removeScoreboard(player);
            setScoreboard(player);
        }
    }

    public void setScoreboard(final @NotNull Player player) {
        BoardManager.getInstance().setupNewBoard(player, new ScoreboardAdapter());
    }

    public String getTitle() {
        return title;
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    public void removeScoreboard(final Player player) {
        BoardManager.getInstance().removeBoard(player);
    }
}
