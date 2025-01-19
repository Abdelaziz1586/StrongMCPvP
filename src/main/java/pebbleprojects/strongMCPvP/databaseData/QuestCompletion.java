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

public final class QuestCompletion {

    public static QuestCompletion INSTANCE;
    private final Map<UUID, Boolean> questCompletion;
    private final String SAVE, SELECT, SELECT_ALL;

    public QuestCompletion() {
        INSTANCE = this;

        questCompletion = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET QUEST_COMPLETION=? WHERE UUID=?";
        SELECT = "SELECT QUEST_COMPLETION FROM PvP WHERE UUID=?";
        SELECT_ALL = "SELECT QUEST_COMPLETION, UUID FROM PvP";
    }

    public void set(final @NotNull UUID uuid, final boolean questCompletion) {
        this.questCompletion.put(uuid, questCompletion);
    }

    public boolean toggle(final @NotNull UUID uuid) {
        return Boolean.TRUE.equals(questCompletion.put(uuid, !get(uuid)));
    }

    public boolean get(final @NotNull UUID uuid) {
        return questCompletion.getOrDefault(uuid, false);
    }

    public boolean search(final @NotNull UUID uuid) throws SQLException {
        if (questCompletion.containsKey(uuid)) return questCompletion.get(uuid);

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    if (UUID.fromString(resultSet.getString("UUID")).equals(uuid)) {
                        final boolean b = resultSet.getBoolean("QUEST_COMPLETION");
                        questCompletion.put(uuid, b);
                        return b;
                    }
                }
            }
        }

        final boolean b = DataHandler.INSTANCE.getData().getBoolean("players." + uuid + ".questCompletion", false);

        questCompletion.put(uuid, b);

        return b;
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                final ResultSet result = select.executeQuery();

                if (result.next()) {
                    questCompletion.put(uuid, result.getBoolean("QUEST_COMPLETION"));
                    return;
                }

                questCompletion.put(uuid, false);
            }

            return;
        }

        questCompletion.put(uuid, DataHandler.INSTANCE.getData().getBoolean("players." + uuid + ".questCompletion", false));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setBoolean(1, get(uuid));
                statement.setString(2, uuid.toString());
                statement.execute();

                questCompletion.remove(uuid);
            }

            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".questCompletion", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}