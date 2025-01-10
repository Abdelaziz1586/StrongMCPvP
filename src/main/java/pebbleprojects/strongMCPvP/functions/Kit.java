package pebbleprojects.strongMCPvP.functions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.DataHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Kit {

    private final int priority;
    private final String permission;
    private final Map<Integer, PvPItem> contents, armorContents;

    public Kit(final Configuration kitSection) {
        contents = new ConcurrentHashMap<>();
        armorContents = new ConcurrentHashMap<>();

        priority = kitSection.getInt("priority", 0);
        permission = kitSection.getString("permission", null);

        final Configuration
                itemsSection = kitSection.getSection("items"),
                armorSection = kitSection.getSection("armor");

        if (itemsSection == null || armorSection == null) return;

        Configuration section;
        for (final String slot : itemsSection.getKeys()) {
            try {
                section = itemsSection.getSection(slot);

                if (section == null) {
                    DataHandler.INSTANCE.getLogger().severe("Item section can not be null!");
                    return;
                }

                contents.put(Integer.parseInt(slot), new PvPItem(section));
            } catch (final NumberFormatException ignored) {
                DataHandler.INSTANCE.getLogger().severe("Index of kits must be numbers!");
            }
        }

        armorContents.put(0, armorSection.contains("helmet") ? new PvPItem(armorSection.getSection("helmet")) : null);
        armorContents.put(1, armorSection.contains("chestplate") ? new PvPItem(armorSection.getSection("chestplate")) : null);
        armorContents.put(2, armorSection.contains("leggings") ? new PvPItem(armorSection.getSection("leggings")) : null);
        armorContents.put(3, armorSection.contains("boots") ? new PvPItem(armorSection.getSection("boots")) : null);
    }

    public int getPriority() {
        return priority;
    }

    public String getPermission() {
        return permission;
    }

    public void applyKit(final Player player, final boolean kill) {
        final PlayerInventory inventory = player.getInventory();

        if (!kill) {
            inventory.setContents(new ItemStack[]{});
            inventory.setArmorContents(new ItemStack[]{});
        }

        for (final Map.Entry<Integer, PvPItem> entry : contents.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItemStack(inventory, kill));
        }

        inventory.setHelmet(armorContents.getOrDefault(0, null).getItemStack(inventory, kill));
        inventory.setChestplate(armorContents.getOrDefault(1, null).getItemStack(inventory, kill));
        inventory.setLeggings(armorContents.getOrDefault(2, null).getItemStack(inventory, kill));
        inventory.setBoots(armorContents.getOrDefault(3, null).getItemStack(inventory, kill));
    }
}
