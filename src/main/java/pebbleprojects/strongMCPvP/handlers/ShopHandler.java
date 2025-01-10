package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pebbleprojects.strongMCPvP.databaseData.PerkSlots;
import pebbleprojects.strongMCPvP.databaseData.Souls;
import pebbleprojects.strongMCPvP.functions.GUI;
import pebbleprojects.strongMCPvP.functions.Perk;
import pebbleprojects.strongMCPvP.functions.PerkSlot;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ShopHandler {

    private GUI shopGUI;
    private Configuration shop;
    private final File shopFile;
    public static ShopHandler INSTANCE;
    private final Map<UUID, GUI> shopGUIClones;

    public ShopHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Shop Handler...");

        INSTANCE = this;
        shopGUIClones = new ConcurrentHashMap<>();

        shopFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "shop.yml");

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Shop Handler!");
    }

    public void update() {
        if (!shopFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("shop.yml", shopFile);
        }

        try {
            shop = ConfigurationProvider.getProvider(YamlConfiguration.class).load(shopFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load shop.yml in memory: " + e.getMessage());
            return;
        }

        final Inventory inventory = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&', shop.getString("guiName", "Shop & Customization")));

        inventory.setItem(10, getDefaultPerksSlot(shop.getSection("perk-slots.1"), 1));
        inventory.setItem(12, getDefaultPerksSlot(shop.getSection("perk-slots.2"), 2));
        inventory.setItem(14, getDefaultPerksSlot(shop.getSection("perk-slots.3"), 3));
        inventory.setItem(16, getDefaultPerksSlot(shop.getSection("perk-slots.4"), 4));

        inventory.setItem(29, GUIHandler.INSTANCE.createItemStack(Material.IRON_SWORD, "§7Temporary Items", null));
        inventory.setItem(31, GUIHandler.INSTANCE.createItemStack(Material.DIAMOND_CHESTPLATE, "§bKits", null));
        inventory.setItem(33, GUIHandler.INSTANCE.createItemStack(Material.ARROW, "§7Arrow Trails", null));

        shopGUI = GUIHandler.INSTANCE.createGUI(inventory, event -> {
            final ItemStack itemStack = event.getClickedItem();

            if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getType() == Material.STAINED_GLASS_PANE)
                return;

            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();
            final int perkSlot = getPerkSlot(event.getSlot());

            if (event.getSlot() == 33) {
                TrailsHandler.INSTANCE.openGUI(player);
                return;
            }

            switch (itemStack.getType()) {
                case BEDROCK:
                    final int price = Integer.parseInt(ChatColor.stripColor(itemStack.getItemMeta().getLore().get(0).split(": ")[1].split(" ")[0]));
                    if (Souls.INSTANCE.remove(uuid, price)) {
                        PerkSlots.INSTANCE.add(uuid, perkSlot);
                        MessageHandler.INSTANCE.sendMessage(player, "perks.slots.buy.success", new String[]{"perkSlot," + perkSlot});

                        openGUI(player, false);
                        return;
                    }

                    MessageHandler.INSTANCE.sendMessage(player, "perks.slots.buy.failed.no-enough-souls", null);
                    break;
                case BARRIER:
                    MessageHandler.INSTANCE.sendMessage(player, "perks.slots.buy.failed.no-permission", new String[]{"rankName," + itemStack.getItemMeta().getLore().get(0).split(" ")[1].split(" §crank!")[0]});
                    break;
                case DIAMOND_CHESTPLATE:
                    KitsHandler.INSTANCE.openGUI(player);
                    break;
                case IRON_SWORD:
                    openGUI(player, true);
                    break;
                default:
                    PerksHandler.INSTANCE.openGUI(player, perkSlot);
                    break;
            }
        });
    }

    public void openGUI(final Player player, final boolean tempItems) {
        if (tempItems) {
            //
            return;
        }

        if (!shopGUIClones.containsKey(player.getUniqueId()))
            shopGUIClones.put(player.getUniqueId(), GUIHandler.INSTANCE.cloneGUI(shopGUI));

        updateShopGUI(player).openGUI(player);
    }

    public Configuration getShop() {
        return shop;
    }

    public int getPerkSlot(final int slot) {
        switch (slot) {
            case 10:
                return 1;
            case 12:
                return 2;
            case 14:
                return 3;
            case 16:
                return 4;
            default:
                return -1;
        }
    }

    public List<Integer> getAvailableSlots(final Player player) {
        final List<Integer> slots = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            if (isSlotAvailable(player, i)) {
                slots.add(i);
            }
        }

        return slots;
    }

    private boolean isSlotAvailable(final Player player, final int slot) {
        final Configuration section = shop.getSection("perk-slots." + slot);

        return section != null && (section.getBoolean("free") || (section.getInt("price", 0) <= 0 && player.hasPermission(section.getString("rank.permission", "*"))));
    }

    private ItemStack getDefaultPerksSlot(final Configuration section, final int slot) {
        return section != null ? GUIHandler.INSTANCE.createItemStack(section.getBoolean("free") ? Material.CLAY_BALL : section.getInt("price") > 0 ? Material.BEDROCK : Material.BARRIER, "§7Perk Slot #" + slot, section.getBoolean("free") ? null : section.getInt("price") > 0 ? Arrays.asList("§7Cost: §b" + section.getInt("price") + " §cSouls", "§bClick to purchase") : Collections.singletonList("§cRequires " + ChatColor.translateAlternateColorCodes('&', section.getString("rank.name")) + " §crank!"))
                : GUIHandler.INSTANCE.createItemStack(Material.CLAY_BALL, "§7Perk Slot #" + slot, null);
    }

    private GUI updateShopGUI(final Player player) {
        final GUI gui = shopGUIClones.get(player.getUniqueId());
        final Inventory inventory = gui.getInventory();

        final List<PerkSlot> perkSlots = PerkSlots.INSTANCE.get(player.getUniqueId());
        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);

            if (i == 10 || i == 12 || i == 14 || i == 16) {
                final int finalI = i;

                final PerkSlot perkSlot = PerkSlots.INSTANCE.get(player.getUniqueId()).stream()
                        .filter(ps -> ps.getPerkSlot() == getPerkSlot(finalI))
                        .findFirst()
                        .orElse(null);

                if (perkSlot != null && perkSlot.getPerkId() == -1) {
                    if (itemStack.getType() == Material.BEDROCK) {
                        if (itemStack.getItemMeta().getLore().size() == 2)
                            inventory.setItem(i, GUIHandler.INSTANCE.createItemStack(Material.CLAY_BALL, "§7Perk Slot #" + perkSlot.getPerkSlot(), null));
                        continue;
                    }

                    inventory.setItem(i, GUIHandler.INSTANCE.createItemStack(Material.CLAY_BALL, "§7Perk Slot #" + perkSlot.getPerkSlot(), null));
                    continue;
                }
            }

            if (itemStack != null && itemStack.getType() != Material.AIR) {
                final int perkSlot = getPerkSlot(i);
                if (perkSlot != -1) {
                    final List<PerkSlot> matchingPerkSlots = perkSlots.stream().filter(perkSlot1 -> perkSlot1.getPerkSlot() == perkSlot).collect(Collectors.toList());

                    if (Objects.requireNonNull(itemStack.getType()) == Material.BARRIER) {
                        if (player.hasPermission(shop.getString("perk-slots." + perkSlot + ".rank.permission", "*")) && !matchingPerkSlots.isEmpty()) {
                            final Perk perk = PerksHandler.INSTANCE.getPerk(matchingPerkSlots.get(0).getPerkId());
                            if (perk != null) {
                                inventory.setItem(i, perk.getGuiItem());
                            } else {
                                inventory.setItem(i, getDefaultPerksSlot(shop.getSection("perk-slots." + perkSlot), perkSlot));
                            }
                        }
                    } else if (!matchingPerkSlots.isEmpty()) {
                        final Perk perk = PerksHandler.INSTANCE.getPerk(matchingPerkSlots.get(0).getPerkId());
                        if (perk != null) {
                            inventory.setItem(i, perk.getGuiItem());
                        } else {
                            inventory.setItem(i, getDefaultPerksSlot(shop.getSection("perk-slots." + perkSlot), perkSlot));
                        }
                    } else {
                        inventory.setItem(i, getDefaultPerksSlot(shop.getSection("perk-slots." + perkSlot), perkSlot));
                    }
                }
            }
        }

        gui.setInventory(inventory);
        return gui;
    }
}
