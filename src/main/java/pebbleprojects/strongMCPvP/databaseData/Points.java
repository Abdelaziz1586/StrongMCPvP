package pebbleprojects.strongMCPvP.databaseData;

import pebbleprojects.strongMCPvP.functions.TopPlayerData;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.handlers.LevelsHandler;
import pebbleprojects.strongMCPvP.handlers.UtilsHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Points {

    public static Points INSTANCE;
    private final Map<UUID, Integer> points;
    private final String SAVE, SELECT, SELECT_ALL;

    public Points() {
        INSTANCE = this;

        points = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET POINTS=? WHERE UUID=?";
        SELECT = "SELECT POINTS FROM PvP WHERE UUID=?";
        SELECT_ALL = "SELECT POINTS FROM PvP";
    }

    public void set(final @NotNull UUID uuid, final int amount) {
        points.put(uuid, amount);

        LevelsHandler.INSTANCE.updatePlayerLevel(uuid);
    }

    public void add(final @NotNull UUID uuid, final int amount) {
        points.put(uuid, get(uuid) + amount);

        LevelsHandler.INSTANCE.updatePlayerLevel(uuid);
    }

    public void remove(final @NotNull UUID uuid, final int amount) {
        points.put(uuid, Math.max(get(uuid) - amount, 0));

        LevelsHandler.INSTANCE.updatePlayerLevel(uuid);
    }

    public int get(final @NotNull UUID uuid) {
        return points.getOrDefault(uuid, 0);
    }

    public int search(final @NotNull UUID uuid) throws SQLException {
        if (points.containsKey(uuid)) return points.get(uuid);

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    if (UUID.fromString(resultSet.getString("UUID")).equals(uuid)) {
                        final int i = resultSet.getInt("POINTS");
                        points.put(uuid, i);
                        return i;
                    }
                }
            }
        }

        final int i = DataHandler.INSTANCE.getData().getInt("players." + uuid + ".points", -1);

        if (i != -1)
            points.put(uuid, i);

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
                        topPlayers.add(new AbstractMap.SimpleEntry<>(new TopPlayerData(uuid, playerName), resultSet.getInt("POINTS")));
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
                        topPlayers.add(new AbstractMap.SimpleEntry<>(new TopPlayerData(uuid, playerName), section.getInt(key + ".points")));
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
                final ResultSet result = select.executeQuery();

                if (result.next()) {
                    points.put(uuid, result.getInt("POINTS"));
                    return;
                }

                points.put(uuid, 0);
            }

            return;
        }

        points.put(uuid, DataHandler.INSTANCE.getData().getInt("players." + uuid + ".points", 0));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setInt(1, get(uuid));
                statement.setString(2, uuid.toString());
                statement.execute();

                points.remove(uuid);
            }

            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".points", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}