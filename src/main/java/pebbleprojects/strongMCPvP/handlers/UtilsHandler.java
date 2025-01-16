package pebbleprojects.strongMCPvP.handlers;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public final class UtilsHandler {

    public static UtilsHandler INSTANCE;
    private final ItemStack nextPage, previousPage;

    public UtilsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Utils Handler...");

        INSTANCE = this;

        nextPage = getHead("MHF_ArrowRight", "§7Next Page", "§7Left Click: Next Page");
        previousPage = getHead("MHF_ArrowLeft", "§7Previous Page", "§7Left Click: Go Back");

        DataHandler.INSTANCE.getLogger().info("Loaded Utils Handler!");
    }

    public ItemStack setUnbreakable(final ItemStack item) {
        final net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(item);

        final NBTTagCompound nbtTagCompound = new NBTTagCompound();

        nbtTagCompound.setInt("Unbreakable", 1);
        itemStack.setTag(nbtTagCompound);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    public void sendTitle(final Player player, final String title, final String subtitle, final int fadeIn, final int stay, final int fadeOut) {
        try {
            sendTitle(player, title, fadeIn, stay, fadeOut);
            sendSubtitle(player, subtitle, fadeIn, stay, fadeOut);
        } catch (final Exception ignored) {
        }
    }

    public void sendActionBar(final Player player, final String message) {
        if (message.isEmpty()) return;

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(ChatColor.translateAlternateColorCodes('&', "{\"text\": \"" + message + "\"}")), (byte) 2));
    }

    public ItemStack[] getNextAndPreviousPage(final int page) {
        final ItemStack next = nextPage.clone(),
                previous = previousPage.clone();

        final ItemMeta nextMeta = next.getItemMeta(),
                previousMeta = previous.getItemMeta();

        nextMeta.getLore().add(1, "§7Current page: " + page);
        previousMeta.getLore().add(1, "§7Current page: " + page);

        next.setItemMeta(nextMeta);
        previous.setItemMeta(previousMeta);

        return new ItemStack[]{next, previous};
    }

    public ItemStack getHead(final String player, final String name, final String... lore) {
        final ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta skull = (SkullMeta) itemStack.getItemMeta();

        skull.setDisplayName(name);
        skull.setOwner(player);

        if (lore != null) {
            skull.setLore(Arrays.asList(lore));
        }

        itemStack.setItemMeta(skull);

        return itemStack;
    }

    private void sendTitle(final Player player, final String title, final int fadeIn, final int stayTime, final int fadeOut) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(title), fadeIn, stayTime, fadeOut));
    }

    private void sendSubtitle(final Player player, final String subtitle, final int fadeIn, final int stayTime, final int fadeOut) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subtitle), fadeIn, stayTime, fadeOut));
    }
}
