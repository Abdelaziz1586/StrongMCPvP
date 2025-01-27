package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pebbleprojects.strongMCPvP.handlers.PerksHandler;

public final class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        PerksHandler.INSTANCE.onPlayerInteract(event);

        final ItemStack itemStack = event.getItem();
        final Block clickedBlock = event.getClickedBlock();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && clickedBlock != null
                && clickedBlock.getType() != Material.AIR
                && itemStack != null
                && itemStack.getType() == Material.FLINT_AND_STEEL
                && itemStack.getDurability() == 63) {
            event.setCancelled(true);

            event.getPlayer().playSound(clickedBlock.getLocation(), Sound.ITEM_BREAK, 1, 1);
        }
    }

}
