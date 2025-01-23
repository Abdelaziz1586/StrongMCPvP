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

public final class Scramble {

    public static Scramble INSTANCE;
    private final Map<UUID, Boolean> scramble;
    private final String SAVE, SELECT, SELECT_ALL;

    public Scramble() {
        INSTANCE = this;

        scramble = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET SCRAMBLE=? WHERE UUID=?";
        SELECT = "SELECT SCRAMBLE FROM PvP WHERE UUID=?";
        SELECT_ALL = "SELECT SCRAMBLE, UUID FROM PvP";
    }

    public void set(final @NotNull UUID uuid, final boolean scramble) {
        this.scramble.put(uuid, scramble);
    }

    public boolean toggle(final @NotNull UUID uuid) {
        final boolean b = !get(uuid);
        scramble.put(uuid, b);

        return b;
    }

    public boolean get(final @NotNull UUID uuid) {
        return scramble.getOrDefault(uuid, false);
    }

    public boolean search(final @NotNull UUID uuid) throws SQLException {
        if (scramble.containsKey(uuid)) return scramble.get(uuid);

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    if (UUID.fromString(resultSet.getString("UUID")).equals(uuid)) {
                        final boolean b = resultSet.getBoolean("SCRAMBLE");
                        scramble.put(uuid, b);
                        return b;
                    }
                }
            }
        }

        final boolean b = DataHandler.INSTANCE.getData().getBoolean("players." + uuid + ".scramble", false);

        scramble.put(uuid, b);

        return b;
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                final ResultSet result = select.executeQuery();

                if (result.next()) {
                    scramble.put(uuid, result.getBoolean("SCRAMBLE"));
                    return;
                }

                scramble.put(uuid, false);
            }

            return;
        }

        scramble.put(uuid, DataHandler.INSTANCE.getData().getBoolean("players." + uuid + ".scramble", false));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setBoolean(1, get(uuid));
                statement.setString(2, uuid.toString());
                statement.execute();

                scramble.remove(uuid);
            }

            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".scramble", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}