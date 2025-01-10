package pebbleprojects.strongMCPvP;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.DatabaseHandler;
import pebbleprojects.strongMCPvP.handlers.NPCHandler;
import pebbleprojects.strongMCPvP.handlers.PacketHandler;
import pebbleprojects.strongMCPvP.handlers.discord.DiscordHandler;

import static org.bukkit.Bukkit.getOnlinePlayers;

public final class PvP extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Loading PvP v1.0...");

        new DataHandler(this);

        getLogger().info("Loaded PvP v1.0!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling PvP v1.0...");

        for (final Player player : getOnlinePlayers()) {
            DatabaseHandler.INSTANCE.save(player);
            PacketHandler.INSTANCE.uninject(player);
        }

        DiscordHandler.INSTANCE.shutdown();

        NPCHandler.INSTANCE.shutdown();

        getLogger().info("Disabled PvP v1.0!");
    }
}
