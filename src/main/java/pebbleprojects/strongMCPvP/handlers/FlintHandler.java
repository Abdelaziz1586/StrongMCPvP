package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class FlintHandler {

    private String actionbar;
    public static FlintHandler INSTANCE;

    public FlintHandler() {
        INSTANCE = this;

        update();

        TaskHandler.INSTANCE.runTaskTimerAsync(() -> {
            for (final Player player : Bukkit.getOnlinePlayers())
                sendActionbar(player);
        }, 1);
    }

    public void update() {
        actionbar = ChatColor.translateAlternateColorCodes('&', MessageHandler.INSTANCE.getMessages().getString("flint-actionbar-message", "&6Flint ✸ &e%durability%"));
    }

    private void sendActionbar(final Player player) {
        final ItemStack itemStack = player.getItemInHand();
        if (itemStack.getType() == Material.FLINT_AND_STEEL)
            UtilsHandler.INSTANCE.sendActionBar(player, actionbar.replace("%durability%", String.valueOf(itemStack.getType().getMaxDurability() - itemStack.getDurability() - 1)));
    }
}
