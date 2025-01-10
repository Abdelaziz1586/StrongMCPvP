package pebbleprojects.strongMCPvP.functions;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

import java.util.function.Consumer;

public final class GUI implements Cloneable {

    private int[] data;
    private Inventory inventory;
    private ContainerWorkbench workbench;
    private final Consumer<GUIClick> clickEventConsumer;

    public GUI(final Inventory inventory, final Consumer<GUIClick> clickEventConsumer, final int[] data) {
        workbench = null;
        this.data = data;
        this.inventory = inventory;
        this.clickEventConsumer = clickEventConsumer;
    }

    public void setData(final int[] data) {
        this.data = data;
    }

    public int[] getData() {
        return data;
    }

    public void setInventory(final Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void openGUI(final Player player) {
        if (inventory.getType() == InventoryType.WORKBENCH) {
            openInventoryViaPacket(player);
            return;
        }

        TaskHandler.INSTANCE.runSync(() -> player.openInventory(inventory));
    }

    public boolean onInventoryClick(final InventoryClickEvent event) {
        if (event.getClickedInventory().equals(inventory)) {
            TaskHandler.INSTANCE.runAsync(() -> clickEventConsumer.accept(new GUIClick((Player) event.getWhoClicked(), event.getSlot(), event.isLeftClick(), event.isRightClick(), event.isShiftClick(), event.getClick() == ClickType.MIDDLE, event.getCurrentItem(), this)));
            return true;
        }

        return false;
    }

    public boolean onPacketInventoryClick(final Player player, final PacketPlayInWindowClick packet) {
        if (workbench != null && packet.a() == workbench.windowId) {
            final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            if (packet.b() > 0) {
                entityPlayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(workbench.windowId, packet.b(), workbench.craftInventory.getItem(packet.b() - 1)));
            }

            if (packet.b() != 5) {
                showItem(entityPlayer);
            }

            entityPlayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, entityPlayer.inventory.getCarried()));

            TaskHandler.INSTANCE.runAsync(() -> clickEventConsumer.accept(new GUIClick(player, packet.b(), packet.c() == 0, packet.c() == 1, packet.c() == 2, false, CraftItemStack.asBukkitCopy(packet.e()), this)));
            return true;
        }

        return false;
    }

    private void openInventoryViaPacket(final Player player) {
        final EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        final int containerId = nmsPlayer.nextContainerCounter();

        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(
                containerId,
                "minecraft:crafting_table",
                new ChatMessage("Crafting Table")
        ));

        if (workbench == null) {
            workbench = new ContainerWorkbench(
                    nmsPlayer.inventory,
                    nmsPlayer.world,
                    new BlockPosition(0, 0, 0)
            );
        }

        workbench.windowId = containerId;
        try {
            workbench.addSlotListener(nmsPlayer);
        } catch (final IllegalArgumentException ignored) {
        }

        for (int i = 0; i < 9; i++) {
            final org.bukkit.inventory.ItemStack bukkitItem = inventory.getItem(i + 1);
            if (bukkitItem != null && bukkitItem.getType() != Material.AIR) {
                org.bukkit.inventory.ItemStack copiedItem = bukkitItem.clone();

                final ItemStack nmsItem = CraftItemStack.asNMSCopy(copiedItem);

                workbench.getSlot(i + 1).set(nmsItem);
                nmsPlayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(containerId, i + 1, nmsItem));
            }
        }

        showItem(nmsPlayer);
    }

    private void showItem(final EntityPlayer nmsPlayer) {
        final org.bukkit.inventory.ItemStack outputItem = inventory.getItem(0);
        if (outputItem != null && outputItem.getType() != Material.AIR) {
            org.bukkit.inventory.ItemStack copiedOutputItem = outputItem.clone();

            final ItemStack nmsOutputItem = CraftItemStack.asNMSCopy(copiedOutputItem);

            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(workbench.windowId, 0, nmsOutputItem));
        }
    }

    public GUI clone() {
        try {
            final GUI clonedGUI = (GUI) super.clone();

            clonedGUI.data = data;

            clonedGUI.inventory = Bukkit.createInventory(null, inventory.getSize(), inventory.getTitle());
            clonedGUI.inventory.setContents(inventory.getContents());

            return clonedGUI;
        } catch (final CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}