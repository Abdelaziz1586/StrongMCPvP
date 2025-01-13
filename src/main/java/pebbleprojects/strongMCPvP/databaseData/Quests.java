package pebbleprojects.strongMCPvP.databaseData;

import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.functions.quests.PlayerQuests;
import pebbleprojects.strongMCPvP.functions.quests.Quest;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;
import pebbleprojects.strongMCPvP.handlers.QuestsHandler;

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
    private final Map<UUID, PlayerQuests> quests;

    public Quests() {
        INSTANCE = this;

        quests = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET QUESTS=? WHERE UUID=?";
        SELECT = "SELECT QUESTS FROM PvP WHERE UUID=?";
    }

    public void add(final @NotNull UUID uuid, final int questId, final long endTime) {
        if (!quests.containsKey(uuid)) return;

        final PlayerQuests playerQuests = quests.get(uuid);

        playerQuests.addQuest(Bukkit.getPlayer(uuid), questId, System.currentTimeMillis(), endTime);

        quests.put(uuid, playerQuests);
    }

    public void remove(final @NotNull UUID uuid, final int questId) {
        if (!quests.containsKey(uuid)) return;

        final PlayerQuests playerQuests = quests.get(uuid);

        playerQuests.removeQuest(questId);

        quests.put(uuid, playerQuests);
    }

    public PlayerQuests get(final @NotNull UUID uuid) {
        return quests.get(uuid);
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        final List<String> quests;
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                final ResultSet result = select.executeQuery();

                quests = result.next() ? DatabaseHandler.INSTANCE.getGson().fromJson(result.getString("QUESTS"), new TypeToken<List<String>>(){}.getType()) : new ArrayList<>();
            }
        } else {
            quests = DataHandler.INSTANCE.getData().getStringList("players." + uuid + ".quests");
        }

        computeQuestsList(uuid, quests);
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                final PlayerQuests playerQuests = get(uuid);

                statement.setString(1, DatabaseHandler.INSTANCE.getGson().toJson(playerQuests.getQuests()));
                statement.setString(2, uuid.toString());
                statement.execute();

                playerQuests.resetQuests();

                quests.remove(uuid);
            }
            return;
        }

        final PlayerQuests playerQuests = get(uuid);

        DataHandler.INSTANCE.getData().set("players." + uuid + ".quests", playerQuests.getQuests());
        DataHandler.INSTANCE.saveData();

        playerQuests.resetQuests();

        quests.remove(uuid);
    }

    private void computeQuestsList(final UUID uuid, final List<String> quests) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        final PlayerQuests playerQuests = new PlayerQuests(uuid);

        final long now = System.currentTimeMillis();
        for (final String data : quests) {
            if (!data.contains(",")) continue;

            final String[] split = data.split(",");
            if (split.length != 3) continue;

            try {
                final int id = Integer.parseInt(split[0]);

                final Quest quest = QuestsHandler.INSTANCE.getQuest(id);
                if (quest == null) continue;

                final long endTime = Long.parseLong(split[1]);
                if (now >= endTime) continue;

                final int currentCounter = Integer.parseInt(split[2]);
                quest.setCounter(player, currentCounter);

                playerQuests.addQuest(player, id, now, endTime);
            } catch (final NumberFormatException ignored) {}
        }

        this.quests.put(uuid, playerQuests);

        QuestsHandler.INSTANCE.setRandomQuests(player);
    }
}