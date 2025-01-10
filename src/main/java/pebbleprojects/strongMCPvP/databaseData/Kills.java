package pebbleprojects.strongMCPvP.databaseData;

import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
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