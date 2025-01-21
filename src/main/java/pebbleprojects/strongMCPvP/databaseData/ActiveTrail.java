package pebbleprojects.strongMCPvP.databaseData;

import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActiveTrail {

    private final String SAVE, SELECT;
    public static ActiveTrail INSTANCE;
    private final Map<UUID, Integer> activeTrails;

    public ActiveTrail() {
        INSTANCE = this;

        activeTrails = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET ACTIVE_TRAIL=? WHERE UUID=?";
        SELECT = "SELECT ACTIVE_TRAIL FROM PvP WHERE UUID=?";
    }

    public void set(final @NotNull UUID uuid, final int slot) {
        activeTrails.put(uuid, slot);
    }

    public int get(final @NotNull UUID uuid) {
        return activeTrails.getOrDefault(uuid, 0);
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                ResultSet result = select.executeQuery();

                if (result.next()) {
                    activeTrails.put(uuid, result.getInt("ACTIVE_TRAIL"));
                    return;
                }

                activeTrails.put(uuid, 0);
            }

            return;
        }

        activeTrails.put(uuid, DataHandler.INSTANCE.getData().getInt("players." + uuid + ".activeTrail", 0));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setInt(1, get(uuid));
                statement.setString(2, uuid.toString());
                statement.execute();

                activeTrails.remove(uuid);
            }

            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".activeTrail", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}