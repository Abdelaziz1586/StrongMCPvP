package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class PlayerCommandPreprocess implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        if (event.getMessage().toLowerCase().startsWith("/reload")) {
            event.setCancelled(true);

            TaskHandler.INSTANCE.runAsync(() -> DataHandler.INSTANCE.update(event.getPlayer()));
        }
    }

}
