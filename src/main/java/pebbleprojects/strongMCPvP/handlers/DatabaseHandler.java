package pebbleprojects.strongMCPvP.handlers;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import pebbleprojects.strongMCPvP.databaseData.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.functions.config.Configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public final class DatabaseHandler {

    private final Gson gson;
    private HikariDataSource hikari;
    public static DatabaseHandler INSTANCE;

    public DatabaseHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Database Handler...");

        INSTANCE = this;

        gson = new Gson();

        update();

        new Perks();
        new Souls();
        new Kills();
        new Quests();
        new Points();
        new Deaths();
        new Trails();
        new Assists();
        new Scramble();
        new RedEffect();
        new PerkSlots();
        new ActiveTrail();
        new QuestReminder();
        new QuestCompletion();

        for (final Player player : DataHandler.INSTANCE.getMain().getServer().getOnlinePlayers())
            load(player);

        DataHandler.INSTANCE.getLogger().info("Loaded Database Handler!");
    }

    public void update() {
        if (hikari != null)
            hikari.close();

        final Configuration database = DataHandler.INSTANCE.getConfig().getSection("database");

        if (database != null && database.getBoolean("enabled", false)) {
            final String host = database.getString("host", null),
                    db = database.getString("database", null),
                    username = database.getString("username", null),
                    password = database.getString("password", null);

            final int port = database.getInt("port", 0);

            if (host == null || port == 0 || db == null || username == null || password == null) {
                hikari = null;
                DataHandler.INSTANCE.getLogger().info("Database data isn't complete, using data file.");

                return;
            }

            hikari = new HikariDataSource();

            hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true");

            hikari.addDataSourceProperty("serverName", host);
            hikari.addDataSourceProperty("port", port);
            hikari.addDataSourceProperty("databaseName", db);
            hikari.addDataSourceProperty("user", username);
            hikari.addDataSourceProperty("password", password);
            hikari.setAutoCommit(true);

            createTables();
            return;
        }

        hikari = null;
        DataHandler.INSTANCE.getLogger().info("No database found or it's not enabled in the config, using data file.");
    }

    public void load(final @NotNull Player player) {
        if (hikari != null) createTables();

        final UUID uuid = player.getUniqueId();

        try {
            Perks.INSTANCE.load(uuid);
            Souls.INSTANCE.load(uuid);
            Kills.INSTANCE.load(uuid);
            Quests.INSTANCE.load(uuid);
            Deaths.INSTANCE.load(uuid);
            Points.INSTANCE.load(uuid);
            Trails.INSTANCE.load(uuid);
            Assists.INSTANCE.load(uuid);
            Scramble.INSTANCE.load(uuid);
            RedEffect.INSTANCE.load(uuid);
            PerkSlots.INSTANCE.load(player);
            ActiveTrail.INSTANCE.load(uuid);
            QuestReminder.INSTANCE.load(uuid);
            QuestCompletion.INSTANCE.load(uuid);

            LevelsHandler.INSTANCE.updatePlayerLevel(uuid);
        } catch (final SQLException e) {
            DataHandler.INSTANCE.getLogger().severe("Error while accessing the database, using data file. Please check the details: " + e.getMessage());
            hikari = null;
        }
    }

    public void save(final @NotNull Player player) {
        if (hikari != null) createTables();

        final UUID uuid = player.getUniqueId();

        try {
            Perks.INSTANCE.save(uuid);
            Souls.INSTANCE.save(uuid);
            Kills.INSTANCE.save(uuid);
            Quests.INSTANCE.save(uuid);
            Deaths.INSTANCE.save(uuid);
            Points.INSTANCE.save(uuid);
            Trails.INSTANCE.save(uuid);
            Assists.INSTANCE.save(uuid);
            Scramble.INSTANCE.save(uuid);
            RedEffect.INSTANCE.save(uuid);
            PerkSlots.INSTANCE.save(uuid);
            ActiveTrail.INSTANCE.save(uuid);
            QuestReminder.INSTANCE.save(uuid);
            QuestCompletion.INSTANCE.save(uuid);
        } catch (final Exception e) {
            DataHandler.INSTANCE.getLogger().severe("Error while accessing the database, using data file. Please check the details: " + e.getMessage());
            hikari = null;
        }
    }

    public Gson getGson() {
        return gson;
    }

    public HikariDataSource getHikari() {
        return hikari;
    }

    private void createTables() {
        try (final Connection connection = hikari.getConnection();
             final Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS PvP(UUID varchar(36) PRIMARY KEY, KILLS int, ASSISTS int, SOULS int, POINTS int, DEATHS int, TRAILS varchar(65), ACTIVE_TRAIL int, SCRAMBLE boolean, RED_EFFECT boolean, QUEST_REMINDER boolean, QUEST_COMPLETION boolean, PERKS TEXT, PERK_SLOTS TEXT, QUESTS TEXT)");
        } catch (final SQLException e) {
            DataHandler.INSTANCE.getLogger().severe("Error while accessing the database, using data file. Please check the details: " + e.getMessage());
            hikari = null;
        }
    }

}
