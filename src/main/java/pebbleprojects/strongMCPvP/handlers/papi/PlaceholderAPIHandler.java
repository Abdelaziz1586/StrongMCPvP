package pebbleprojects.strongMCPvP.handlers.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

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
        return sender instanceof Player && hasPAPI ? PlaceholderAPI.setPlaceholders((Player) sender, text) : text;
    }
}
