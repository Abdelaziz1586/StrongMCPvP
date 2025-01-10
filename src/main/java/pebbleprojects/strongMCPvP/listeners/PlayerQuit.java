package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pebbleprojects.strongMCPvP.handlers.GameHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        TaskHandler.INSTANCE.runAsync(() -> GameHandler.INSTANCE.leave(event.getPlayer()));
    }

}
