package pebbleprojects.strongMCPvP.functions.profile;

import pebbleprojects.strongMCPvP.databaseData.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import pebbleprojects.strongMCPvP.handlers.KillStreakHandler;
import pebbleprojects.strongMCPvP.handlers.discord.ProfileHandler;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Profile {

    private UUID uuid;
    private boolean scramble;
    private String query, error;
    private RealProfile realProfile;
    private int souls, kills, deaths, points, assists;

    public Profile(final String query) {
        this.query = query;
    }

    public void loadWithQuery() {
        realProfile = ProfileHandler.INSTANCE.getRealProfile(query);

        final List<OfflinePlayer> offlinePlayers = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(offlinePlayer -> offlinePlayer.getName().equalsIgnoreCase(query))
                .collect(Collectors.toList());

        if (offlinePlayers.isEmpty()) {
            setErrorValues();
            return;
        }

        for (final OfflinePlayer offlinePlayer : offlinePlayers) {
            if (!loadWithUUID(offlinePlayer.getUniqueId())) {
                query = offlinePlayer.getName();
                return;
            }
        }
    }

    private boolean loadWithUUID(final UUID uuid) {
        this.uuid = uuid;

        try {
            souls = Souls.INSTANCE.search(uuid);
            kills = Kills.INSTANCE.search(uuid);
            deaths = Deaths.INSTANCE.search(uuid);
            points = Points.INSTANCE.search(uuid);
            assists = Assists.INSTANCE.search(uuid);

            scramble = Scramble.INSTANCE.search(uuid);

            error = null;

            return souls == -1 || kills == -1 || deaths == -1 || points == -1 || assists == -1;
        } catch (final SQLException e) {
            error = e.getMessage();
            setErrorValues();
            return true;
        }
    }

    public String replaceStringWithData(final String string, final boolean scrambleIfPossible) {
        return string == null ? null : string
                .replace("%player%", getQuery())
                .replace("%query%", getQuery())
                .replace("%souls%", getSouls(scrambleIfPossible))
                .replace("%kills%", getKills())
                .replace("%deaths%", getDeaths())
                .replace("%assists%", getAssists())
                .replace("%points%", getPoints(scrambleIfPossible))
                .replace("%uuid%", uuid != null ? uuid.toString() : getRealUUID())
                .replace("%realUUID%", getRealUUID())
                .replace("%error%", error != null ? error : "")
                .replace("%killStreak%", uuid == null ? "0" : String.valueOf(KillStreakHandler.INSTANCE.getKillStreaks(uuid)));
    }

    public String getRealUUID() {
        return realProfile.getUUID() == null ? realProfile.getName() : realProfile.getUUID().toString();
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

    private String getQuery() {
        return realProfile.getName();
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
