package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import pebbleprojects.strongMCPvP.handlers.BlocksHandler;

public final class BlockFromTo implements Listener {

    @EventHandler
    public void onBlockFromTo(final BlockFromToEvent event) {
        if (BlocksHandler.INSTANCE.isCached(event.getBlock().getLocation()))
            event.setCancelled(true);
    }

}
