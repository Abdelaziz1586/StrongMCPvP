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

public final class Kills {

    public static Kills INSTANCE;
    private final Map<UUID, Integer> kills;
    private final String SAVE, SELECT, SELECT_ALL;

    public Kills() {
        INSTANCE = this;

        kills = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET KILLS=? WHERE UUID=?";
        SELECT = "SELECT KILLS FROM PvP WHERE UUID=?";
        SELECT_ALL = "SELECT KILLS FROM PvP";
    }

    public void set(final @NotNull UUID uuid, final int amount) {
        kills.put(uuid, amount);
    }

    public void add(final @NotNull UUID uuid, final int amount) {
        kills.put(uuid, get(uuid) + amount);
    }

    public void remove(final @NotNull UUID uuid, final int amount) {
        kills.put(uuid, Math.max(get(uuid) - amount, 0));
    }

    public int get(final @NotNull UUID uuid) {
        return kills.getOrDefault(uuid, 0);
    }

    public int search(final @NotNull UUID uuid) throws SQLException {
        if (kills.containsKey(uuid)) return kills.get(uuid);

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    if (UUID.fromString(resultSet.getString("UUID")).equals(uuid)) {
                        final int i = resultSet.getInt("KILLS");
                        kills.put(uuid, i);
                        return i;
                    }
                }
            }
        }

        final int i = DataHandler.INSTANCE.getData().getInt("players." + uuid + ".kills", -1);

        if (i != -1) {
            kills.put(uuid, i);
        }

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
                        topPlayers.add(new AbstractMap.SimpleEntry<>(new TopPlayerData(uuid, playerName), resultSet.getInt("KILLS")));
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
                        topPlayers.add(new AbstractMap.SimpleEntry<>(new TopPlayerData(uuid, playerName), section.getInt(key + ".kills")));
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
                    kills.put(uuid, result.getInt("KILLS"));
                    return;
                }

                kills.put(uuid, 0);
            }
            return;
        }

        kills.put(uuid, DataHandler.INSTANCE.getData().getInt("players." + uuid + ".kills", 0));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setInt(1, get(uuid));
                statement.setString(2, uuid.toString());
                statement.execute();

                kills.remove(uuid);
            }

            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".kills", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}