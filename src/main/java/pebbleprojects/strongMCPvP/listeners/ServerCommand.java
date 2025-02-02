package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class ServerCommand implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(final ServerCommandEvent event) {
        if (event.getCommand().toLowerCase().startsWith("reload")) {
            event.setCancelled(true);

            TaskHandler.INSTANCE.runAsync(() -> DataHandler.INSTANCE.update(event.getSender()));
        }
    }
}
