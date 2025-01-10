package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pebbleprojects.strongMCPvP.handlers.GameHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        TaskHandler.INSTANCE.runAsync(() -> GameHandler.INSTANCE.join(event.getPlayer()));
    }

}
