package pebbleprojects.strongMCPvP.databaseData;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Perks {

    public static Perks INSTANCE;
    private final String SAVE, SELECT;
    private final Map<UUID, List<Integer>> perks;

    public Perks() {
        INSTANCE = this;

        perks = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET PERKS=? WHERE UUID=?";
        SELECT = "SELECT PERKS FROM PvP WHERE UUID=?";
    }

    public void add(final @NotNull UUID uuid, final int perkId) {
        final List<Integer> perks = this.perks.getOrDefault(uuid, new ArrayList<>());

        if (perks.contains(perkId)) return;

        perks.add(perkId);

        this.perks.put(uuid, perks);
    }

    public void remove(final @NotNull UUID uuid, final int perkId) {
        final List<Integer> perks = this.perks.getOrDefault(uuid, new ArrayList<>());

        if (!perks.contains(perkId)) return;

        perks.remove(perkId);

        this.perks.put(uuid, perks);
    }

    public List<Integer> get(final @NotNull UUID uuid) {
        return perks.getOrDefault(uuid, new ArrayList<>());
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                final ResultSet result = select.executeQuery();

                if (result.next()) {
                    perks.put(uuid, DatabaseHandler.INSTANCE.getGson().fromJson(result.getString("PERKS"), new TypeToken<List<Integer>>(){}.getType()));
                    return;
                }

                perks.put(uuid, new ArrayList<>());
            }
            return;
        }

        perks.put(uuid, DataHandler.INSTANCE.getData().getIntList("players." + uuid + ".perks"));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setString(1, DatabaseHandler.INSTANCE.getGson().toJson(get(uuid)));
                statement.setString(2, uuid.toString());
                statement.execute();

                perks.remove(uuid);
            }
            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".perks", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}