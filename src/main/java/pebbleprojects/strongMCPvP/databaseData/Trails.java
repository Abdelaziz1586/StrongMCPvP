package pebbleprojects.strongMCPvP.databaseData;

import com.google.common.reflect.TypeToken;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class  Trails {

    public static Trails INSTANCE;
    private final String SAVE, SELECT;
    private final Map<UUID, List<Integer>> arrowTrails;

    public Trails() {
        INSTANCE = this;

        arrowTrails = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET TRAILS=? WHERE UUID=?";
        SELECT = "SELECT TRAILS FROM PvP WHERE UUID=?";
    }

    public void add(final @NotNull UUID uuid, final int arrowTrailSlot) {
        final List<Integer> arrowTrails = this.arrowTrails.getOrDefault(uuid, new ArrayList<>());

        if (arrowTrails.contains(arrowTrailSlot)) return;

        arrowTrails.add(arrowTrailSlot);

        this.arrowTrails.put(uuid, arrowTrails);
    }

    public void remove(final @NotNull UUID uuid, final int arrowTrailSlot) {
        final List<Integer> arrowTrails = this.arrowTrails.getOrDefault(uuid, new ArrayList<>());

        if (!arrowTrails.contains(arrowTrailSlot)) return;

        arrowTrails.remove(arrowTrailSlot);

        this.arrowTrails.put(uuid, arrowTrails);
    }

    public List<Integer> get(final @NotNull UUID uuid) {
        return arrowTrails.getOrDefault(uuid, new ArrayList<>());
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                final ResultSet result = select.executeQuery();

                if (result.next()) {
                    arrowTrails.put(uuid, DatabaseHandler.INSTANCE.getGson().fromJson(result.getString("TRAILS"), new TypeToken<List<Integer>>(){}.getType()));
                    return;
                }

                arrowTrails.put(uuid, new ArrayList<>());
            }
            return;
        }

        arrowTrails.put(uuid, DataHandler.INSTANCE.getData().getIntList("players." + uuid + ".trails"));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setString(1, DatabaseHandler.INSTANCE.getGson().toJson(get(uuid)));
                statement.setString(2, uuid.toString());
                statement.execute();

                arrowTrails.remove(uuid);
            }
            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".trails", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}