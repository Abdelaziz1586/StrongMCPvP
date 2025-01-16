package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pebbleprojects.strongMCPvP.databaseData.Souls;
import pebbleprojects.strongMCPvP.functions.GUI;
import pebbleprojects.strongMCPvP.functions.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class TempItemShopHandler {

    private final List<GUI> guis;
    public static TempItemShopHandler INSTANCE;
    private final Map<Integer, Map<Integer, TemporarilyItem>> temporarilyItems;

    public TempItemShopHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Temporarily Item Shop Handler...");

        INSTANCE = this;

        guis = new ArrayList<>();

        temporarilyItems = new ConcurrentHashMap<>();

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Temporarily Item Shop Handler!");
    }

    public void update() {
        guis.clear();

        final String guiName = ChatColor.translateAlternateColorCodes('&', ShopHandler.INSTANCE.getShop().getString("guiName", "&e&lPvP Shop"));

        final List<Inventory> inventories = new ArrayList<>();
        inventories.add(Bukkit.createInventory(null, 9, guiName));

        int i = 0;
        int currentInventory = 0;

        final Configuration section = ShopHandler.INSTANCE.getShop().getSection("items");
        if (section == null) return;

        final Map<Integer, TemporarilyItem> map = new ConcurrentHashMap<>();
        for (final String key : section.getKeys()) {
            final Configuration itemSection = section.getSection(key);
            if (itemSection == null) continue;

            final Material material = Material.getMaterial(itemSection.getString("material", "").toUpperCase());
            if (material == null) continue;

            if (i >= 8) {
                i = 1;
                currentInventory++;
                if (currentInventory >= inventories.size()) {
                    inventories.add(Bukkit.createInventory(null, 9, guiName));
                }

                final ItemStack[] items = UtilsHandler.INSTANCE.getNextAndPreviousPage(currentInventory + 1);
                inventories.get(currentInventory - 1).setItem(8, items[0]);
                inventories.get(currentInventory).setItem(0, items[1]);

                temporarilyItems.put(currentInventory - 1, map);
                map.clear();
            }

            final TemporarilyItem temporarilyItem = new TemporarilyItem(material, itemSection);
            map.put(i, temporarilyItem);

            inventories.get(currentInventory).setItem(i, temporarilyItem.guiItem);
            i++;
        }

        temporarilyItems.put(currentInventory, map);

        for (int i2 = 0; i2 < inventories.size(); i2++) {
            final int finalI = i2;
            guis.add(GUIHandler.INSTANCE.createGUI(inventories.get(finalI), event -> {
                final ItemStack itemStack = event.getClickedItem();

                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    if (event.getSlot() == 0 && finalI > 0) {
                        guis.get(finalI - 1).openGUI(event.getPlayer());
                        return;
                    }

                    if (event.getSlot() == 8) {
                        if (guis.size() >= finalI + 2) {
                            guis.get(finalI + 1).openGUI(event.getPlayer());
                        }
                        return;
                    }
                }

                final TemporarilyItem temporarilyItem = temporarilyItems.get(finalI).get(event.getSlot());
                if (temporarilyItem == null) return;

                giveItem(event.getPlayer(), temporarilyItem);
            }));
        }
    }

    public void openGUI(final Player player) {
        if (guis.isEmpty()) return;

        guis.get(0).openGUI(player);
    }

    private void giveItem(final Player player, final TemporarilyItem temporarilyItem) {
        if (Souls.INSTANCE.remove(player.getUniqueId(), temporarilyItem.price)) {
            if (!temporarilyItem.canBuyMultiple && hasItem(player, temporarilyItem.itemStack)) {
                MessageHandler.INSTANCE.sendMessage(player, "temporary-shop-items.failed.can-not-buy-multiple", new String[]{"query," + temporarilyItem.queryName});
                return;
            }

            handleItem(player, temporarilyItem.itemStack);
            MessageHandler.INSTANCE.sendMessage(player, "temporary-shop-items.success", new String[]{"query," + temporarilyItem.queryName});
            return;
        }

        MessageHandler.INSTANCE.sendMessage(player, "temporary-shop-items.failed.no-enough-souls", new String[]{"query," + temporarilyItem.queryName});
    }

    private void handleItem(final Player player, final ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return;

        switch (getArmorType(item)) {
            case HELMET:
                equipArmor(player, item, 0);
                break;
            case CHESTPLATE:
                equipArmor(player, item, 1);
                break;
            case LEGGINGS:
                equipArmor(player, item, 2);
                break;
            case BOOTS:
                equipArmor(player, item, 3);
                break;
            default:
                player.getInventory().addItem(item);
                break;
        }
    }

    private void equipArmor(final Player player, final ItemStack item, final int armorSlot) {
        switch (armorSlot) {
            case 0:
                final ItemStack currentHelmet = player.getInventory().getHelmet();
                if (currentHelmet == null || currentHelmet.getType() == Material.AIR || isBetterArmor(item, currentHelmet)) {
                    if (currentHelmet != null && currentHelmet.getType() != Material.AIR)
                        player.getInventory().addItem(currentHelmet);

                    player.getInventory().setHelmet(item);
                    break;
                }

                player.getInventory().addItem(item);
                break;
            case 1:
                final ItemStack currentChestplate = player.getInventory().getChestplate();
                if (currentChestplate == null || currentChestplate.getType() == Material.AIR || isBetterArmor(item, currentChestplate)) {
                    if (currentChestplate != null && currentChestplate.getType() != Material.AIR)
                        player.getInventory().addItem(currentChestplate);

                    player.getInventory().setChestplate(item);
                    break;
                }

                player.getInventory().addItem(item);
                break;
            case 2:
                final ItemStack currentLeggings = player.getInventory().getLeggings();
                if (currentLeggings == null || currentLeggings.getType() == Material.AIR || isBetterArmor(item, currentLeggings)) {
                    if (currentLeggings != null && currentLeggings.getType() != Material.AIR)
                        player.getInventory().addItem(currentLeggings);

                    player.getInventory().setLeggings(item);
                    break;
                }

                player.getInventory().addItem(item);
                break;
            case 3:
                final ItemStack currentBoots = player.getInventory().getBoots();
                if (currentBoots == null || currentBoots.getType() == Material.AIR || isBetterArmor(item, currentBoots)) {
                    if (currentBoots != null && currentBoots.getType() != Material.AIR)
                        player.getInventory().addItem(currentBoots);

                    player.getInventory().setBoots(item);
                    break;
                }

                player.getInventory().addItem(item);
                break;
            default:
                throw new IllegalArgumentException("Invalid armor slot: " + armorSlot);
        }
    }

    private boolean isBetterArmor(final ItemStack newArmor, final ItemStack currentArmor) {
        return newArmor.getType().getMaxDurability() > currentArmor.getType().getMaxDurability() || getTotalEnchantmentLevel(newArmor) > getTotalEnchantmentLevel(currentArmor);
    }

    private int getTotalEnchantmentLevel(final ItemStack item) {
        return item.getEnchantments().values().stream().mapToInt(Integer::intValue).sum();
    }

    private ArmorType getArmorType(final ItemStack item) {
        final String typeName = item.getType().name();
        if (typeName.endsWith("_HELMET")) return ArmorType.HELMET;
        if (typeName.endsWith("_CHESTPLATE")) return ArmorType.CHESTPLATE;
        if (typeName.endsWith("_LEGGINGS")) return ArmorType.LEGGINGS;
        if (typeName.endsWith("_BOOTS")) return ArmorType.BOOTS;
        return ArmorType.NONE;
    }

    private boolean hasItem(final Player player, final ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;

        for (final ItemStack invItem : player.getInventory().getContents())
            if (invItem != null && invItem.isSimilar(item))
                return true;

        for (final ItemStack armorItem : player.getInventory().getArmorContents())
            if (armorItem != null && armorItem.isSimilar(item))
                return true;

        return false;
    }


    private enum ArmorType {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS, NONE
    }

    private static final class TemporarilyItem {

        private final int price;
        private final String queryName;
        private final boolean canBuyMultiple;
        private final ItemStack itemStack, guiItem;

        private TemporarilyItem(final Material material, final Configuration section) {
            price = Math.max(section.getInt("price", 0), 0);

            canBuyMultiple = section.getBoolean("canBuyMultipleOf");

            queryName = ChatColor.translateAlternateColorCodes('&', section.getString("queryName", ""));

            guiItem = new ItemStack(material);

            ItemMeta itemMeta = guiItem.getItemMeta();
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("gui.name", "")));
            itemMeta.setLore(section.getStringList("gui.lore").stream().map(value -> ChatColor.translateAlternateColorCodes('&', value)).collect(Collectors.toList()));

            guiItem.setItemMeta(itemMeta);

            Configuration enchantmentsSection = section.getBoolean("gui.enchantments.enabled", false) ? section.getSection("gui.enchantments.enchantments") : null;

            if (enchantmentsSection != null) {
                for (final String enchantment : enchantmentsSection.getKeys()) {
                    try {
                        guiItem.addUnsafeEnchantment(Enchantment.getByName(enchantment), enchantmentsSection.getInt(enchantment, 1));
                    } catch (final IllegalArgumentException | NullPointerException ignored) {
                        DataHandler.INSTANCE.getLogger().severe("Couldn't find enchantment named " + enchantment);
                    }
                }
            }

            itemStack = new ItemStack(material, Math.max(section.getInt("amount", 1), 1));

            itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("item.name", "")));
            itemMeta.setLore(section.getBoolean("item.lore.enabled") ? section.getStringList("item.lore.lore").stream().map(value -> ChatColor.translateAlternateColorCodes('&', value)).collect(Collectors.toList()) : new ArrayList<>());

            itemStack.setItemMeta(itemMeta);

            enchantmentsSection = section.getBoolean("item.enchantments.enabled", false) ? section.getSection("item.enchantments.enchantments") : null;

            if (enchantmentsSection != null) {
                for (final String enchantment : enchantmentsSection.getKeys()) {
                    try {
                        itemStack.addUnsafeEnchantment(Enchantment.getByName(enchantment), enchantmentsSection.getInt(enchantment, 1));
                    } catch (final IllegalArgumentException | NullPointerException ignored) {
                        DataHandler.INSTANCE.getLogger().severe("Couldn't find enchantment named " + enchantment);
                    }
                }
            }
        }
    }
}
