package pebbleprojects.strongMCPvP;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pebbleprojects.strongMCPvP.handlers.*;
import pebbleprojects.strongMCPvP.handlers.discord.DiscordHandler;
import pebbleprojects.strongMCPvP.handlers.luckperms.LuckPermsHandler;

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

        final PacketHandler packets = PacketHandler.INSTANCE;
        final DatabaseHandler database = DatabaseHandler.INSTANCE;
        for (final Player player : getOnlinePlayers()) {
            database.save(player);
            packets.uninject(player);
        }

        DiscordHandler.INSTANCE.shutdown();

        NPCHandler.INSTANCE.shutdown();
        LuckPermsHandler.INSTANCE.shutdown();
        LeaderboardHandler.INSTANCE.shutdown();

        getLogger().info("Disabled PvP v1.0!");
    }
}
