package pebbleprojects.strongMCPvP.handlers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import pebbleprojects.strongMCPvP.databaseData.PerkSlots;
import pebbleprojects.strongMCPvP.databaseData.Perks;
import pebbleprojects.strongMCPvP.databaseData.Souls;
import pebbleprojects.strongMCPvP.functions.GUI;
import pebbleprojects.strongMCPvP.functions.perks.Perk;
import pebbleprojects.strongMCPvP.functions.perks.PerkSlot;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class PerksHandler {

    private GUI perksGUI;
    private Configuration perks;
    private final File perksFile;
    public static PerksHandler INSTANCE;
    private final Map<Integer, Perk> perksList;
    private final Map<UUID, GUI> perksGUIClones;
    private final Map<Integer, Integer> slotIds;
    private final Map<UUID, List<Perk>> playerPerks;

    public PerksHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Perks Handler...");

        INSTANCE = this;

        slotIds = new ConcurrentHashMap<>();
        perksList = new ConcurrentHashMap<>();
        playerPerks = new ConcurrentHashMap<>();
        perksGUIClones = new ConcurrentHashMap<>();

        perksFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "perks.yml");

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Perks Handler!");
    }

    public void update() {
        if (!perksFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("perks.yml", perksFile);
        }

        try {
            perks = ConfigurationProvider.getProvider(YamlConfiguration.class).load(perksFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load perks.yml in memory: " + e.getMessage());
        }

        final File perksDirectory = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "perks");

        if (perksDirectory.mkdir()) {
            DataHandler.INSTANCE.copyToPluginDirectory("perks/Frost.yml", new File(perksDirectory, "Frost.yml"));
            DataHandler.INSTANCE.copyToPluginDirectory("perks/GoldenHead.yml", new File(perksDirectory, "GoldenHead.yml"));
            DataHandler.INSTANCE.copyToPluginDirectory("perks/EndlessQuiver.yml", new File(perksDirectory, "EndlessQuiver.yml"));
        }

        final File[] perks = perksDirectory.listFiles();

        if (perks == null) return;

        slotIds.clear();
        perksList.clear();
        for (final File file : perks) {
            try {
                final Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

                final int id = config.getInt("id"),
                        price = config.getInt("price");
                final String textures,
                        name = ChatColor.translateAlternateColorCodes('&', config.getString("name", "")),
                        materialString = config.getString("material");

                if (name.isEmpty()) {
                    DataHandler.INSTANCE.getLogger().warning("Unable to load perk " + file.getName() + " in memory: Couldn't fetch 'name' key from configuration!");
                    continue;
                }

                if (materialString == null) {
                    DataHandler.INSTANCE.getLogger().warning("Unable to load perk " + file.getName() + " in memory: Couldn't fetch 'material' key from configuration!");
                    continue;
                }
                
                final Material material;
                if (materialString.startsWith("[") && materialString.endsWith("]")) {
                    final String[] materialParts = materialString.split("\\[")[1].split("]")[0].split(";", 2);
                    if (materialParts.length != 2 || !materialParts[0].equalsIgnoreCase("SKULL_ITEM")) {
                        DataHandler.INSTANCE.getLogger().warning("Unable to load perk " + file.getName() + " in memory: Can't recognize material '" + materialString + "'");
                        continue;
                    }

                    textures = materialParts[1];
                    material = Material.SKULL_ITEM;
                } else {
                    textures = null;
                    material = Material.getMaterial(materialString.toUpperCase());
                }

                if (material == null) {
                    DataHandler.INSTANCE.getLogger().warning("Unable to load perk " + file.getName() + " in memory: Can't recognize material '" + materialString + "'");
                    continue;
                }
                
                if (perksList.containsKey(id)) {
                    DataHandler.INSTANCE.getLogger().warning("Unable to load perk " + file.getName() + " in memory: Duplicate IDs");
                    continue;
                }

                if (id < 0) {
                    DataHandler.INSTANCE.getLogger().warning("Unable to load perk " + file.getName() + " in memory: IDs must be greater than 0");
                    continue;
                }

                final ItemStack guiItem = new ItemStack(material);
                final ItemMeta meta = guiItem.getItemMeta();

                meta.setDisplayName(name);
                meta.setLore(config.getStringList("lore").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
                if (textures != null) {
                    guiItem.setDurability((short) 3);

                    final SkullMeta skull = (SkullMeta) meta;

                    skull.setOwner("Notch");
                    final GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                    profile.getProperties().put("textures", new Property("textures", textures));

                    try {
                        final Field profileField = skull.getClass().getDeclaredField("profile");
                        profileField.setAccessible(true);
                        profileField.set(skull, profile);
                    } catch (final NoSuchFieldException | IllegalAccessException e) {
                        throw new Error(e);
                    }

                    guiItem.setItemMeta(skull);
                } else {
                    guiItem.setItemMeta(meta);
                }

                perksList.put(id, new Perk(price, guiItem, config));
            } catch (final IOException e) {
                DataHandler.INSTANCE.getLogger().warning("Unable to load perk " + file.getName() + " in memory: " + e.getMessage());
            }
        }

        updateGUI();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            setPerk(player);
        }
    }

    private void updateGUI() {
        final Inventory inventory = Bukkit.createInventory(null, 9 * Math.min(perksList.size(), 1), ChatColor.translateAlternateColorCodes('&', perks.getString("guiName", "Perk Customization")));

        int i = 0;
        for (final Map.Entry<Integer, Perk> entry : perksList.entrySet()) {
            inventory.addItem(entry.getValue().getGuiItem());

            slotIds.put(i, entry.getKey());
            i++;
        }

        perksGUI = GUIHandler.INSTANCE.createGUI(inventory, event -> {
            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();

            final short[] data = event.getGUI().getData();
            if (data == null || data.length == 0) {
                return;
            }

            final PerkSlot perkSlot = PerkSlots.INSTANCE.get(uuid).stream()
                    .filter(ps -> ps.getPerkSlot() == data[0])
                    .findFirst()
                    .orElse(null);

            if (perkSlot == null) return;

            final ItemStack itemStack = event.getClickedItem();
            final ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta == null || itemMeta.getLore() == null || itemMeta.getLore().size() < 3) return;

            final String s = itemMeta.getLore().get(2);
            final int perkId = slotIds.get(event.getSlot());
            if (s.contains("unequip")) {
                perkSlot.setPerkId(-1);
                MessageHandler.INSTANCE.sendMessage(player, "perks.perk.unequip", new String[]{"perk," + itemMeta.getDisplayName(), "perkSlot," + perkSlot.getPerkSlot()});

                ShopHandler.INSTANCE.openGUI(player);
                return;
            }

            if (s.contains("equip")) {
                for (final PerkSlot slot : PerkSlots.INSTANCE.get(uuid)) {
                    if (slot.getPerkId() == perkId)
                        slot.setPerkId(-1);
                }

                perkSlot.setPerkId(perkId);
                MessageHandler.INSTANCE.sendMessage(player, "perks.perk.equip", new String[]{"perk," + itemMeta.getDisplayName(), "perkSlot," + perkSlot.getPerkSlot()});

                ShopHandler.INSTANCE.openGUI(player);
                return;
            }

            final int price = perksList.get(perkId).getPrice();
            if (Souls.INSTANCE.remove(uuid, price)) {
                perkSlot.setPerkId(perkId);
                Perks.INSTANCE.add(uuid, perkId);

                MessageHandler.INSTANCE.sendMessage(player, "perks.perk.buy.success", new String[]{"perk," + itemMeta.getDisplayName(), "perkSlot," + perkSlot.getPerkSlot()});

                ShopHandler.INSTANCE.openGUI(player);
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "perks.perk.buy.no-enough-souls", new String[]{"perk," + itemMeta.getDisplayName(), "perkSlot," + perkSlot.getPerkSlot()});
        });
    }

    public Perk getPerk(final int id) {
        return perksList.get(id);
    }

    public void openGUI(final Player player, final int perkSlot) {
        if (!perksGUIClones.containsKey(player.getUniqueId()))
            perksGUIClones.put(player.getUniqueId(), GUIHandler.INSTANCE.cloneGUI(perksGUI));

        updatePerksGUI(player, perkSlot).openGUI(player);
    }

    private GUI updatePerksGUI(final Player player, final int perkSlotId) {
        final UUID uuid = player.getUniqueId();
        final GUI gui = perksGUIClones.get(uuid);
        final Inventory inventory = gui.getInventory();

        final List<Integer> perks = Perks.INSTANCE.get(uuid);
        final PerkSlot perkSlot = PerkSlots.INSTANCE.get(uuid).stream()
                .filter(ps -> ps.getPerkSlot() == perkSlotId)
                .findFirst()
                .orElse(null);

        if (perkSlot != null) {
            int i = 0;
            for (final Map.Entry<Integer, Perk> entry : perksList.entrySet()) {
                final ItemStack itemStack = inventory.getItem(i);
                if (itemStack == null) continue;

                final ItemMeta itemMeta = itemStack.getItemMeta();
                final List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();

                while (lore.size() <= 2) {
                    lore.add("");
                }

                if (!"§7".equals(lore.get(1))) {
                    lore.set(1, "§7");
                }

                if (perkSlot.getPerkId() == entry.getKey()) {
                    lore.set(2, "§a§l➜ §aClick to §cunequip.");
                } else if (perks.contains(entry.getKey())) {
                    lore.set(2, "§a§l➜ §aClick to equip.");
                } else {
                    final int diff = entry.getValue().getPrice() - Souls.INSTANCE.get(uuid);

                    if (diff >= 0) {
                        lore.set(2, "§c§l➜ §cYou are missing " + diff + " souls.");
                    } else {
                        lore.set(2, "§7§l➜ §7Click to purchase for " + entry.getValue().getPrice() + " souls.");
                    }
                }

                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);

                i++;
            }
        }

        gui.setData(new short[]{(short) perkSlotId});
        return gui;
    }

    public void setPerk(final Player player) {
        final List<Perk> perks = new ArrayList<>();
        for (final PerkSlot perkSlot : PerkSlots.INSTANCE.get(player.getUniqueId())) {
            if (perkSlot.getPerkId() != -1 && perksList.containsKey(perkSlot.getPerkId())) {
                perks.add(perksList.get(perkSlot.getPerkId()));
            }
        }

        playerPerks.put(player.getUniqueId(), perks);
    }

    public void onEntityDamage(final EntityDamageEvent event) {
        Player attacker = null;
        for (final Perk perk : playerPerks.getOrDefault(event.getEntity().getUniqueId(), new ArrayList<>()))
            attacker = attacker == null ? perk.onEntityDamage(event) : attacker;

        if (attacker != null)
            QuestsHandler.INSTANCE.onKill(attacker);
    }

    public void onPlayerSpawn(final Player player) {
        for (final Perk perk : playerPerks.getOrDefault(player.getUniqueId(), new ArrayList<>()))
            perk.onPlayerSpawn(player);
    }

    public void onPlayerDeath(final Player victim, final Player attacker) {
        for (final Perk perk : playerPerks.getOrDefault(victim.getUniqueId(), new ArrayList<>()))
            perk.onPlayerDeath(victim, attacker);

        QuestsHandler.INSTANCE.onDeath(victim);
    }

    public void onPlayerInteract(final PlayerInteractEvent event) {
        for (final Perk perk : playerPerks.getOrDefault(event.getPlayer().getUniqueId(), new ArrayList<>()))
            perk.onPlayerClick(event);
    }
}
