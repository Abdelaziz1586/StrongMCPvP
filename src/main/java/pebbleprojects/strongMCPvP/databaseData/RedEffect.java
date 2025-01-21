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

public final class RedEffect {

    public static RedEffect INSTANCE;
    private final Map<UUID, Boolean> redEffect;
    private final String SAVE, SELECT, SELECT_ALL;

    public RedEffect() {
        INSTANCE = this;

        redEffect = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET RED_EFFECT=? WHERE UUID=?";
        SELECT = "SELECT RED_EFFECT FROM PvP WHERE UUID=?";
        SELECT_ALL = "SELECT RED_EFFECT, UUID FROM PvP";
    }

    public void set(final @NotNull UUID uuid, final boolean redEffect) {
        this.redEffect.put(uuid, redEffect);
    }

    public boolean toggle(final @NotNull UUID uuid) {
        return Boolean.TRUE.equals(redEffect.put(uuid, !get(uuid)));
    }

    public boolean get(final @NotNull UUID uuid) {
        return redEffect.getOrDefault(uuid, false);
    }

    public boolean search(final @NotNull UUID uuid) throws SQLException {
        if (redEffect.containsKey(uuid)) return redEffect.get(uuid);

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    if (UUID.fromString(resultSet.getString("UUID")).equals(uuid)) {
                        final boolean b = resultSet.getBoolean("RED_EFFECT");
                        redEffect.put(uuid, b);
                        return b;
                    }
                }
            }
        }

        final boolean b = DataHandler.INSTANCE.getData().getBoolean("players." + uuid + ".redEffect", false);

        redEffect.put(uuid, b);

        return b;
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                try (final ResultSet result = select.executeQuery()) {
                    if (result.next()) {
                        redEffect.put(uuid, result.getBoolean("RED_EFFECT"));
                        return;
                    }
                }

                redEffect.put(uuid, false);
            }
            return;
        }

        redEffect.put(uuid, DataHandler.INSTANCE.getData().getBoolean("players." + uuid + ".redEffect", false));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setBoolean(1, get(uuid));
                statement.setString(2, uuid.toString());
                statement.execute();

                redEffect.remove(uuid);
            }

            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".redEffect", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}