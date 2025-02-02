package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import pebbleprojects.strongMCPvP.handlers.PerksHandler;

public final class BlockPlace implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        PerksHandler.INSTANCE.onBlockPlace(event);
    }

}
