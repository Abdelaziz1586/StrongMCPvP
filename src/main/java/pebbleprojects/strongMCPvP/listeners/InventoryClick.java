package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import pebbleprojects.strongMCPvP.handlers.GUIHandler;

public final class InventoryClick implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getClickedInventory() != null && GUIHandler.INSTANCE.onInventoryClick(event)) {
            event.setCancelled(true);
        }
    }

}
