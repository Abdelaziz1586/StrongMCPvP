package pebbleprojects.strongMCPvP.databaseData;

import pebbleprojects.strongMCPvP.functions.TopPlayerData;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.handlers.UtilsHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Souls {

    public static Souls INSTANCE;
    private final Map<UUID, Integer> souls;
    private final String SAVE, SELECT, INSERT, SELECT_ALL;

    public Souls() {
        INSTANCE = this;

        souls = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET SOULS=? WHERE UUID=?";
        SELECT = "SELECT SOULS FROM PvP WHERE UUID=?";
        INSERT = "INSERT INTO PvP(UUID, SOULS) VALUES (?, ?)";
        SELECT_ALL = "SELECT SOULS, UUID FROM PvP";
    }

    public void set(final @NotNull UUID uuid, final int amount) {
        souls.put(uuid, amount);
    }

    public void add(final @NotNull UUID uuid, final int amount) {
        souls.put(uuid, get(uuid) + amount);
    }

    public boolean remove(final @NotNull UUID uuid, final int amount) {
        final int current = get(uuid);

        if (current >= amount) {
            souls.put(uuid, current - amount);
            return true;
        }

        return false;
    }

    public int get(final @NotNull UUID uuid) {
        return souls.getOrDefault(uuid, 0);
    }

    public int search(final @NotNull UUID uuid) throws SQLException {
        if (souls.containsKey(uuid)) return souls.get(uuid);

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    if (UUID.fromString(resultSet.getString("UUID")).equals(uuid)) {
                        final int i = resultSet.getInt("SOULS");
                        souls.put(uuid, i);
                        return i;
                    }
                }
            }
        }

        final int i = DataHandler.INSTANCE.getData().getInt("players." + uuid + ".souls", -1);

        if (i != -1) souls.put(uuid, i);

        return i;
    }

    public List<Map.Entry<TopPlayerData, Integer>> getTop10Players() throws SQLException {
        List<Map.Entry<TopPlayerData, Integer>> topPlayers = new ArrayList<>();

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    final UUID uuid = UUID.fromString(resultSet.getString("UUID"));
                    final String playerName = UtilsHandler.INSTANCE.getPlayerNameByUUID(uuid);

                    if (playerName != null)
                        topPlayers.add(new AbstractMap.SimpleEntry<>(new TopPlayerData(uuid, playerName), souls.containsKey(uuid) ? souls.get(uuid) : resultSet.getInt("SOULS")));
                }

            } catch (final SQLException e) {
                throw new SQLException("Error retrieving top players from the database.", e);
            }
        } else {
            final Configuration section = DataHandler.INSTANCE.getData().getSection("players");
            if (section != null) {
                for (final String key : section.getKeys()) {
                    final UUID uuid = UUID.fromString(key);
                    final String playerName = UtilsHandler.INSTANCE.getPlayerNameByUUID(uuid);

                    if (playerName != null)
                        topPlayers.add(new AbstractMap.SimpleEntry<>(new TopPlayerData(uuid, playerName), souls.containsKey(uuid) ? souls.get(uuid) : section.getInt(key + ".souls")));
                }
            }
        }

        topPlayers.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));

        if (topPlayers.size() > 10)
            topPlayers = topPlayers.subList(0, 10);

        return topPlayers;
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                ResultSet result = select.executeQuery();

                if (result.next()) {
                    souls.put(uuid, result.getInt("SOULS"));
                    return;
                }

                souls.put(uuid, 0);
            }

            return;
        }

        souls.put(uuid, DataHandler.INSTANCE.getData().getInt("players." + uuid + ".souls", 0));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE);
                 final PreparedStatement insertStatement = connection.prepareStatement(INSERT)) {
                statement.setInt(1, get(uuid));
                statement.setString(2, uuid.toString());

                if (statement.executeUpdate() == 0) {
                    insertStatement.setString(1, uuid.toString());
                    insertStatement.setInt(2, get(uuid));
                    insertStatement.executeUpdate();
                }

                souls.remove(uuid);
            }
            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".souls", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}