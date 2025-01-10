package pebbleprojects.strongMCPvP.databaseData;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.functions.PerkSlot;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class PerkSlots {

    public static PerkSlots INSTANCE;
    private final String SAVE, SELECT;
    private final Map<UUID, List<PerkSlot>> perkSlots;

    public PerkSlots() {
        INSTANCE = this;

        perkSlots = new ConcurrentHashMap<>();

        SAVE = "UPDATE PvP SET PERK_SLOTS=? WHERE UUID=?";
        SELECT = "SELECT PERK_SLOTS FROM PvP WHERE UUID=?";
    }

    public void add(final @NotNull UUID uuid, final int perkSlot) {
        final List<PerkSlot> perks = this.perkSlots.getOrDefault(uuid, new ArrayList<>());

        if (perks.stream().map(PerkSlot::getPerkSlot).anyMatch(perkSlotDataSlot -> perkSlotDataSlot == perkSlot)) return;

        perks.add(new PerkSlot(-1, perkSlot));

        this.perkSlots.put(uuid, perks);
    }

    public List<PerkSlot> get(final @NotNull UUID uuid) {
        return perkSlots.get(uuid);
    }

    public void load(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement select = connection.prepareStatement(SELECT)) {
                select.setString(1, uuid.toString());
                final ResultSet result = select.executeQuery();

                if (result.next()) {
                    result.getString("PERK_SLOTS");

                    perkSlots.put(uuid, getPerkSlots(DatabaseHandler.INSTANCE.getGson().fromJson(result.getString("GUILD_MEMBERS"), new TypeToken<List<String>>() {
                    }.getType())));
                    return;
                }

                perkSlots.put(uuid, Collections.singletonList(new PerkSlot(-1, 1)));
            }
            return;
        }

        perkSlots.put(uuid,
                DataHandler.INSTANCE.getData().getStringList("players." + uuid + ".perkSlots").stream()
                        .map(PerkSlot::new)
                        .collect(Collectors.toList()));
    }

    public void save(final @NotNull UUID uuid) throws SQLException {
        if (DatabaseHandler.INSTANCE.getHikari() != null) {
            try (final Connection connection = DatabaseHandler.INSTANCE.getHikari().getConnection();
                 final PreparedStatement statement = connection.prepareStatement(SAVE)) {
                statement.setString(1, DatabaseHandler.INSTANCE.getGson().toJson(get(uuid).stream().map(PerkSlot::toString).collect(Collectors.toList())));
                statement.setString(2, uuid.toString());
                statement.execute();

                perkSlots.remove(uuid);
            }
            return;
        }

        DataHandler.INSTANCE.getData().set("players." + uuid + ".perkSlots", get(uuid).stream().map(PerkSlot::toString).collect(Collectors.toList()));
        DataHandler.INSTANCE.saveData();
    }

    private List<PerkSlot> getPerkSlots(final List<String> perkSlots) {
        return perkSlots.stream().map(PerkSlot::new).collect(Collectors.toList());
    }
}