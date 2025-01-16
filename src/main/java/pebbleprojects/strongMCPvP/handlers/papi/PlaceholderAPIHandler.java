package pebbleprojects.strongMCPvP.handlers.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

import java.util.UUID;

public final class PlaceholderAPIHandler {

    private final boolean hasPAPI;
    public static PlaceholderAPIHandler INSTANCE;

    public PlaceholderAPIHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading PlaceholderAPI Handler...");

        INSTANCE = this;

        hasPAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        if (hasPAPI) {
            TaskHandler.INSTANCE.runSync(() -> new PlaceholderExpansionHandler().register());

            DataHandler.INSTANCE.getLogger().info("Found PlaceholderAPI and connected successfully!");
        }

        DataHandler.INSTANCE.getLogger().info("Loaded PlaceholderAPI Handler!");
    }

    public String translateMessage(final CommandSender sender, final String text) {
        return sender instanceof Player && hasPAPI ? ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders((Player) sender, text)) : text;
    }

    public String translateMessage(final UUID uuid, final String text) {
        if (!hasPAPI) return text;

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer == null || !offlinePlayer.hasPlayedBefore() ? text : ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(offlinePlayer, text));
    }

}
