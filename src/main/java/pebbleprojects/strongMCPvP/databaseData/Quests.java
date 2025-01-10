package pebbleprojects.strongMCPvP.databaseData;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Quests {

    public static Quests INSTANCE;
    private final String SAVE, SELECT;
    private final Map<UUID, List<Integer>> quests;

    public Quests() {
        INSTANCE = this;

        quests = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET QUESTS=? WHERE UUID=?";
        SELECT = "SELECT QUESTS FROM PvP WHERE UUID=?";
    }

    public void add(final @NotNull UUID uuid, final int questId) {
        final List<Integer> quests = this.quests.getOrDefault(uuid, new ArrayList<>());

        if (quests.contains(questId)) return;

        quests.add(questId);

        this.quests.put(uuid, quests);
    }

    public void remove(final @NotNull UUID uuid, final int questId) {
        final List<Integer> quests = this.quests.getOrDefault(uuid, new ArrayList<>());

        if (!quests.contains(questId)) return;

        quests.remove(questId);

        this.quests.put(uuid, quests);
    }

    public List<Integer> get(final @NotNull UUID uuid) {
        return quests.getOrDefault(uuid, new ArrayList<>());
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                final ResultSet result = select.executeQuery();

                if (result.next()) {
                    quests.put(uuid, DatabaseHandler.INSTANCE.getGson().fromJson(result.getString("QUESTS"), new TypeToken<List<Integer>>(){}.getType()));
                    return;
                }

                quests.put(uuid, new ArrayList<>());
            }
            return;
        }

        quests.put(uuid, DataHandler.INSTANCE.getData().getIntList("players." + uuid + ".quests"));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setString(1, DatabaseHandler.INSTANCE.getGson().toJson(get(uuid)));
                statement.setString(2, uuid.toString());
                statement.execute();

                quests.remove(uuid);
            }
            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".quests", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}