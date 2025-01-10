package pebbleprojects.strongMCPvP.functions;

import pebbleprojects.strongMCPvP.databaseData.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import pebbleprojects.strongMCPvP.handlers.discord.ProfileHandler;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Profile {

    private UUID uuid;
    private boolean scramble;
    private String query, error;
    private int souls, kills, deaths, points, assists;

    public Profile(final String query) {
        this.query = query;
    }

    public void loadWithQuery() {
        final Map<String, OfflinePlayer> playerMap = Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                .collect(Collectors.toMap(
                        player -> player.getName().toLowerCase(),
                        player -> player,
                        (existing, replacement) -> existing
                ));

        final OfflinePlayer player = playerMap.get(query.toLowerCase());

        if (player == null) {
            setErrorValues();
            return;
        }

        this.query = player.getName();

        loadWithUUID(player.getUniqueId());
    }

    public void loadWithUUID(final UUID uuid) {
        this.uuid = uuid;

        try {
            souls = Souls.INSTANCE.search(uuid);
            kills = Kills.INSTANCE.search(uuid);
            deaths = Deaths.INSTANCE.search(uuid);
            points = Points.INSTANCE.search(uuid);
            assists = Assists.INSTANCE.search(uuid);

            scramble = Scramble.INSTANCE.search(uuid);

            error = null;
        } catch (final SQLException e) {
            error = e.getMessage();
            setErrorValues();
        }
    }

    public String replaceStringWithData(final String string, final boolean scrambleIfPossible) {
        return string == null ? null : string
                .replace("%player%", query)
                .replace("%query%", query)
                .replace("%souls%", getSouls(scrambleIfPossible))
                .replace("%kills%", getKills())
                .replace("%deaths%", getDeaths())
                .replace("%assists%", getAssists())
                .replace("%points%", getPoints(scrambleIfPossible))
                .replace("%uuid%", ProfileHandler.INSTANCE.getRealUUID(query))
                .replace("%error%", error != null ? error : "");
//                .replace("%killStreak%", uuid == null ? "0" : String.valueOf(KillsHandler.INSTANCE.getKillStreaks(uuid)));
    }

    public String getQuery() {
        return query;
    }

    public String getError() {
        return error;
    }

    public String getSouls(final boolean scrambleIfPossible) {
        return scrambleIfPossible ? scramble ? "#" : String.valueOf(souls) : String.valueOf(souls);
    }

    public String getKills() {
        return String.valueOf(kills);
    }

    public String getAssists() {
        return String.valueOf(assists);
    }

    public String getDeaths() {
        return String.valueOf(deaths);
    }

    public String getPoints(final boolean scrambleIfPossible) {
        return scrambleIfPossible ? scramble ? "#" : String.valueOf(points) : String.valueOf(points);
    }

    private void setErrorValues() {
        souls = -1;
        kills = -1;
        deaths = -1;
        points = -1;
        assists = -1;
        scramble = false;
    }
}
