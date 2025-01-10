package pebbleprojects.strongMCPvP.databaseData;

import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Assists {

    public static Assists INSTANCE;
    private final Map<UUID, Integer> assists;
    private final String SAVE, SELECT, SELECT_ALL;

    public Assists() {
        INSTANCE = this;

        assists = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET ASSISTS=? WHERE UUID=?";
        SELECT = "SELECT ASSISTS FROM PvP WHERE UUID=?";
        SELECT_ALL = "SELECT ASSISTS FROM PvP";
    }

    public void set(final @NotNull UUID uuid, final int amount) {
        assists.put(uuid, amount);
    }

    public void add(final @NotNull UUID uuid, final int amount) {
        assists.put(uuid, get(uuid) + amount);
    }

    public void remove(final @NotNull UUID uuid, final int amount) {
        assists.put(uuid, Math.max(get(uuid) - amount, 0));
    }

    public int get(final @NotNull UUID uuid) {
        return assists.getOrDefault(uuid, 0);
    }

    public int search(final @NotNull UUID uuid) throws SQLException {
        if (assists.containsKey(uuid)) return assists.get(uuid);

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    if (UUID.fromString(resultSet.getString("UUID")).equals(uuid)) {
                        final int i = resultSet.getInt("ASSISTS");
                        assists.put(uuid, i);
                        return i;
                    }
                }
            }
        }

        final int i = DataHandler.INSTANCE.getData().getInt("players." + uuid + ".assists", -1);

        if (i != -1) {
            assists.put(uuid, i);
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
                    assists.put(uuid, result.getInt("ASSISTS"));
                    return;
                }

                assists.put(uuid, 0);
            }
            return;
        }

        assists.put(uuid, DataHandler.INSTANCE.getData().getInt("players." + uuid + ".assists", 0));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setInt(1, get(uuid));
                statement.setString(2, uuid.toString());
                statement.execute();

                assists.remove(uuid);
            }

            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".assists", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}