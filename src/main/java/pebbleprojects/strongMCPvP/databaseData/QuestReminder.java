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

public final class QuestReminder {

    public static QuestReminder INSTANCE;
    private final Map<UUID, Boolean> questReminder;
    private final String SAVE, SELECT, SELECT_ALL;

    public QuestReminder() {
        INSTANCE = this;

        questReminder = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET QUEST_REMINDER=? WHERE UUID=?";
        SELECT = "SELECT QUEST_REMINDER FROM PvP WHERE UUID=?";
        SELECT_ALL = "SELECT QUEST_REMINDER FROM PvP";
    }

    public void set(final @NotNull UUID uuid, final boolean questReminder) {
        this.questReminder.put(uuid, questReminder);
    }

    public boolean toggle(final @NotNull UUID uuid) {
        return Boolean.TRUE.equals(questReminder.put(uuid, !get(uuid)));
    }

    public boolean get(final @NotNull UUID uuid) {
        return questReminder.getOrDefault(uuid, false);
    }

    public boolean search(final @NotNull UUID uuid) throws SQLException {
        if (questReminder.containsKey(uuid)) return questReminder.get(uuid);

        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement selectAll = connection.prepareStatement(SELECT_ALL);
                 final ResultSet resultSet = selectAll.executeQuery()) {

                while (resultSet.next()) {
                    if (UUID.fromString(resultSet.getString("UUID")).equals(uuid)) {
                        final boolean b = resultSet.getBoolean("QUEST_REMINDER");
                        questReminder.put(uuid, b);
                        return b;
                    }
                }
            }
        }

        final boolean b = DataHandler.INSTANCE.getData().getBoolean("players." + uuid + ".questReminder", false);

        questReminder.put(uuid, b);

        return b;
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                final ResultSet result = select.executeQuery();

                if (result.next()) {
                    questReminder.put(uuid, result.getBoolean("QUEST_REMINDER"));
                    return;
                }

                questReminder.put(uuid, false);
            }

            return;
        }

        questReminder.put(uuid, DataHandler.INSTANCE.getData().getBoolean("players." + uuid + ".questReminder", false));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setBoolean(1, get(uuid));
                statement.setString(2, uuid.toString());
                statement.execute();

                questReminder.remove(uuid);
            }

            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".questReminder", get(uuid));
        DataHandler.INSTANCE.saveData();
    }
}