package pebbleprojects.strongMCPvP.functions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.UtilsHandler;

import java.util.List;

public final class PvPItem {

    private final int amount;
    private final short durability;
    private final ItemStack itemStack;

    public PvPItem(final Configuration section) {
        itemStack = createItemStack(section);

        amount = section.getInt("increase-amount-by-kill", -1);
        durability = section.getShort("increase-durability-by-kill", (short) -1);
    }

    public ItemStack getItemStack(final PlayerInventory inventory, final boolean kill) {
        if (kill) {
            if (amount == -1 && durability == -1) return itemStack;

            ItemStack itemStack = getItemSlot(inventory, this.itemStack);

            if (itemStack == null) itemStack = this.itemStack.clone();

            if (amount > 0) {
                itemStack.setAmount(Math.min(itemStack.getAmount() + amount, itemStack.getMaxStackSize()));
            } else if (amount == 0) {
                itemStack.setAmount(this.itemStack.getAmount());
            }

            if (durability > 0) {
                itemStack.setDurability((short) (itemStack.getDurability() - durability));
            }

            return itemStack;
        }

        return itemStack;
    }

    private ItemStack getItemSlot(final PlayerInventory inventory, final ItemStack itemStack) {
        for (final ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == itemStack.getType()) {
                return item;
            }
        }

        return null;
    }

    private ItemStack createItemStack(final Configuration section) {
        if (!section.contains("material") || !section.contains("name") || !section.contains("amount") || !section.contains("durability") || !section.contains("lore") || !section.contains("unbreakable") || !section.contains("enchantments")) {
            return null;
        }

        try {
            ItemStack itemStack = new ItemStack(Material.valueOf(section.getString("material")));

            if (section.getBoolean("unbreakable", false)) {
                itemStack = UtilsHandler.INSTANCE.setUnbreakable(itemStack);
            }

            itemStack.setAmount(section.getInt("amount", 1));

            final ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta == null) return null;

            final short durability = section.getShort("durability", (short) -1);

            if (durability > 0) {
                itemStack.setDurability(durability);
            }

            final List<String> lore = section.getStringList("lore");

            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));

            itemMeta.setLore(lore);
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name", itemMeta.getDisplayName())));

            final Configuration enchantmentsSection = section.getBoolean("enchantments.enabled", false) ? section.getSection("enchantments.enchantments") : null;

            itemStack.setItemMeta(itemMeta);

            if (enchantmentsSection != null) {
                for (final String enchantment : enchantmentsSection.getKeys()) {
                    try {
                        itemStack.addUnsafeEnchantment(Enchantment.getByName(enchantment), enchantmentsSection.getInt(enchantment, 1));
                    } catch (final IllegalArgumentException | NullPointerException ignored) {
                        DataHandler.INSTANCE.getLogger().severe("Couldn't find enchantment named " + enchantment);
                        return null;
                    }
                }
            }

            return itemStack;
        } catch (final IllegalArgumentException ignored) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find material named " + section.getSection("material"));
            return null;
        }
    }

}
