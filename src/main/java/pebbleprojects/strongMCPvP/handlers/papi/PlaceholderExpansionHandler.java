package pebbleprojects.strongMCPvP.handlers.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.databaseData.*;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.KillStreakHandler;

public final class PlaceholderExpansionHandler extends PlaceholderExpansion {

    public static PlaceholderExpansionHandler INSTANCE;

    public PlaceholderExpansionHandler() {
        INSTANCE = this;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "PebbleProjects";
    }

    @Override
    public @NotNull String getVersion() {
        return DataHandler.INSTANCE.getMain().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(final @NotNull OfflinePlayer player, final @NotNull String params) {
        switch (params.toLowerCase()) {
            case "souls":
                return String.valueOf(Souls.INSTANCE.get(player.getUniqueId()));
            case "kills":
                return String.valueOf(Kills.INSTANCE.get(player.getUniqueId()));
            case "deaths":
                return String.valueOf(Deaths.INSTANCE.get(player.getUniqueId()));
            case "bounty":
                return String.valueOf(KillStreakHandler.INSTANCE.getBounty(player.getUniqueId()));
            case "points":
                return String.valueOf(Points.INSTANCE.get(player.getUniqueId()));
            case "assists":
                return String.valueOf(Assists.INSTANCE.get(player.getUniqueId()));
            case "killstreak":
                return String.valueOf(KillStreakHandler.INSTANCE.getKillStreaks(player.getUniqueId()));
            default:
                return null;
        }
    }
}
