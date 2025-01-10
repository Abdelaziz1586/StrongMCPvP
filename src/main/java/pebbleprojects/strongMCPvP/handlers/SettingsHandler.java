package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pebbleprojects.strongMCPvP.databaseData.Scramble;
import pebbleprojects.strongMCPvP.functions.GUI;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public final class SettingsHandler {

    private final GUI settingsGUI;
    private Configuration settings;
    private final File settingsFile;
    private final List<UUID> delays;
    public static SettingsHandler INSTANCE;
    private final Map<UUID, GUI> settingsGUIClones;

    public SettingsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Settings Handler...");

        INSTANCE = this;

        delays = new CopyOnWriteArrayList<>();
        settingsGUIClones = new ConcurrentHashMap<>();

        settingsFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "settings.yml");

        final Inventory inventory = Bukkit.createInventory(null, 27, "Player Settings");

        inventory.setItem(10, GUIHandler.INSTANCE.createItemStack(Material.BOOK_AND_QUILL, "§cRank Scramble §7(§cDISABLED§7)", Arrays.asList("§7This option shows/hides", "§7your rank to/from others."), ItemFlag.HIDE_ENCHANTS));
        inventory.setItem(11, GUIHandler.INSTANCE.createItemStack(Material.GOLD_BLOCK, "§cRank Toggle §7(§cDISABLED§7)", Arrays.asList("§7This option shows/hides", "§7your rank to/from others.", "§7", "§7§l» §7Current: %player%"), ItemFlag.HIDE_ENCHANTS));
        inventory.setItem(13, GUIHandler.INSTANCE.createItemStack(Material.WATCH, "§eTime", Arrays.asList("§7", "§7Sunrise", "§7Morning", "§a§l» §7Noon", "§7Night", "§7Midnight", "§7", "§b§l➜ §bClick to cycle time"), ItemFlag.HIDE_ENCHANTS));
        inventory.setItem(15, GUIHandler.INSTANCE.createItemStack(Material.PAPER, "§cQuest Reminder §7(§cDISABLED§7)", Arrays.asList("§7This option reminds you", "§7of your quests when you", "§7join the server."), ItemFlag.HIDE_ENCHANTS));
        inventory.setItem(16, GUIHandler.INSTANCE.createItemStack(Material.BOOK, "§cQuest Completion Alerts §7(§cDISABLED§7)", Arrays.asList("§7This option toggles the title", "§7that appears when you", "§7complete a quest."), ItemFlag.HIDE_ENCHANTS));

        settingsGUI = GUIHandler.INSTANCE.createGUI(inventory, event -> {
            final ItemStack itemStack = event.getClickedItem();

            if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getType() == Material.STAINED_GLASS_PANE)
                return;

            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();

            final ItemMeta meta = itemStack.getItemMeta();
            switch (itemStack.getType()) {
                case BOOK_AND_QUILL:
                    Scramble.INSTANCE.set(uuid, meta.getEnchants().isEmpty());
                    MessageHandler.INSTANCE.sendMessage(player, "settings.scramble." + (meta.getEnchants().isEmpty() ? "enable" : "disable"), null);

                    openGUI(player);
                    break;
                case GOLD_BLOCK:
                    TaskHandler.INSTANCE.runSync(() -> {
                        player.performCommand("t");
                        openGUI(player);
                    });
                    break;
                case WATCH:
                    handleChangeTime(player, itemStack);
                    openGUI(player);
                    break;
            }
        });

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Settings Handler...");
    }

    public void update() {
        if (!settingsFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("settings.yml", settingsFile);
        }

        try {
            settings = ConfigurationProvider.getProvider(YamlConfiguration.class).load(settingsFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load settings.yml in memory: " + e.getMessage());
        }
    }

    public Configuration getSettings() {
        return settings;
    }

    public void openGUI(final Player player) {
        if (!settingsGUIClones.containsKey(player.getUniqueId()))
            settingsGUIClones.put(player.getUniqueId(), GUIHandler.INSTANCE.cloneGUI(settingsGUI));

        updateShopGUI(player).openGUI(player);
    }

    private GUI updateShopGUI(final Player player) {
        final UUID uuid = player.getUniqueId();

        final GUI gui = settingsGUIClones.get(uuid);
        final Inventory inventory = gui.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);

            if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getType() != Material.STAINED_GLASS_PANE) {
                final ItemMeta meta = itemStack.getItemMeta();
                meta.setLore(meta.getLore().stream().map(line -> line.replace("%player%", player.getDisplayName())).collect(Collectors.toList()));

                switch (itemStack.getType()) {
                    case BOOK_AND_QUILL:
                        if (Scramble.INSTANCE.get(uuid)) {
                            meta.addEnchant(Enchantment.DURABILITY, 1, true);
                            meta.setDisplayName("§aRank Scramble §7(§aENABLED§7)");
                            break;
                        }

                        meta.removeEnchant(Enchantment.DURABILITY);
                        meta.setDisplayName("§cRank Scramble §7(§cDISABLED§7)");
                        break;
//                    case PAPER:
//
                }

                itemStack.setItemMeta(meta);
            }
        }

        gui.setInventory(inventory);

        return gui;
    }

    private void handleChangeTime(final Player player, final ItemStack itemStack) {
        if (delays.contains(player.getUniqueId())) {
            MessageHandler.INSTANCE.sendMessage(player, "settings.time.failed", null);
            return;
        }

        final ItemMeta meta = itemStack.getItemMeta();
        final List<String> lore = meta.getLore();

        for (int i = 0; i < lore.size(); i++) {
            final String line = lore.get(i);
            if (line.startsWith("§a§l»")) {
                final int next = i == 5 ? 1 : i + 1;

                final long time;
                final String timeName;
                switch (next) {
                    case 2:
                        time = 1000;
                        timeName = "Morning";
                        break;
                    case 3:
                        time = 6000;
                        timeName = "Noon";
                        break;
                    case 4:
                        time = 12000;
                        timeName = "Night";
                        break;
                    case 5:
                        time = 18000;
                        timeName = "Midnight";
                        break;
                    default:
                        time = 0;
                        timeName = "Sunrise";
                        break;
                }

                lore.set(i, line.replace("§a§l» ", ""));
                lore.set(next, "§a§l» " + lore.get(next));

                meta.setLore(lore);
                itemStack.setItemMeta(meta);

                delays.add(player.getUniqueId());

                TaskHandler.INSTANCE.runAsync(() -> {
                    player.setPlayerTime(time, false);
                    delays.remove(player.getUniqueId());
                });

                MessageHandler.INSTANCE.sendMessage(player, "settings.time.success", new String[]{"time," + timeName});
                return;
            }
        }
    }

}
