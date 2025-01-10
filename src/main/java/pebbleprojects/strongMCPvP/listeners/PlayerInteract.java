package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pebbleprojects.strongMCPvP.handlers.PerksHandler;

public final class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        PerksHandler.INSTANCE.onPlayerInteract(event);
    }

}
