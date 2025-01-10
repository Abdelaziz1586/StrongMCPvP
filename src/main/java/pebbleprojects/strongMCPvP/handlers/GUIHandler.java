package pebbleprojects.strongMCPvP.handlers;

import net.minecraft.server.v1_8_R3.PacketPlayInWindowClick;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pebbleprojects.strongMCPvP.functions.GUI;
import pebbleprojects.strongMCPvP.functions.GUIClick;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class GUIHandler {

    private final List<GUI> guis;
    public static GUIHandler INSTANCE;

    public GUIHandler() {
        INSTANCE = this;

        guis = new ArrayList<>();
    }

    public GUI createGUI(final Inventory inventory, final Consumer<GUIClick> clickEventConsumer, final short... data) {
        final GUI gui = new GUI(inventory, clickEventConsumer, data);

        guis.add(gui);

        return gui;
    }

    public GUI cloneGUI(final GUI gui) {
        if (guis.contains(gui)) {
            final GUI clone = gui.clone();

            guis.add(clone);

            return clone;
        }

        return null;
    }

    public ItemStack createItemStack(final Material material, final String name, final List<String> lore, final ItemFlag... itemFlags) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        if (name != null)
            meta.setDisplayName(name);

        if (lore != null)
            meta.setLore(lore);

        if (itemFlags != null)
            meta.addItemFlags(itemFlags);

        item.setItemMeta(meta);

        return item;
    }

    public boolean onInventoryClick(final InventoryClickEvent event) {
        for (final GUI gui : guis) {
            if (gui.onInventoryClick(event)) return true;
        }

        return false;
    }

    public boolean onPacketInventoryClick(final Player player, final PacketPlayInWindowClick packet) {
        for (final GUI gui : guis) {
            final boolean b = gui.onPacketInventoryClick(player, packet);

            if (b) return true;
        }

        return false;
    }

}
