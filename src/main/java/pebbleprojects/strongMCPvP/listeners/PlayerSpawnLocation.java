package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import pebbleprojects.strongMCPvP.handlers.NPCHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class PlayerSpawnLocation implements Listener {

    @EventHandler
    public void onPlayerSpawnLocation(final PlayerSpawnLocationEvent event) {
        TaskHandler.INSTANCE.runLaterAsync(() -> NPCHandler.INSTANCE.loadNPCs(event.getPlayer()), 1);
    }

}
