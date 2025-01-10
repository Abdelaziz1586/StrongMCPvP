package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import pebbleprojects.strongMCPvP.databaseData.ActiveTrail;
import pebbleprojects.strongMCPvP.databaseData.Souls;
import pebbleprojects.strongMCPvP.databaseData.Trails;
import pebbleprojects.strongMCPvP.functions.GUI;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.NoteColor;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TrailsHandler {

    private GUI projectiles;
    private Configuration trails;
    private final File trailsFile;
    public static TrailsHandler INSTANCE;
    private final Map<UUID, GUI> projectileClone;

    public TrailsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Trails Handler...");

        INSTANCE = this;

        projectileClone = new ConcurrentHashMap<>();

        trailsFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "trails.yml");

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Trails Handler!");
    }

    public void update() {
        if (!trailsFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("trails.yml", trailsFile);
        }

        try {
            trails = ConfigurationProvider.getProvider(YamlConfiguration.class).load(trailsFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load trails.yml in memory: " + e.getMessage());
            return;
        }

        final Configuration trails = this.trails.getSection("gui");

        if (trails == null) {
            DataHandler.INSTANCE.getLogger().severe("Couldn't find 'gui' section in trails.yml! Rebuilding file.");

            trailsFile.delete();

            update();
            return;
        }

        final Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', trails.getString("name", "&e&lProjectile Trails")));

        ItemStack item = GUIHandler.INSTANCE.createItemStack(Material.STAINED_GLASS_PANE, "§7", null);

        item.setDurability((short) 10);

        inventory.setItem(0, item);
        inventory.setItem(1, item);
        inventory.setItem(3, item);

        inventory.setItem(4, GUIHandler.INSTANCE.createItemStack(Material.ARROW, "§7Projectile Trails", Collections.singletonList("§7Currently set to default")));

        inventory.setItem(5, item);
        inventory.setItem(7, item);
        inventory.setItem(8, item);
        inventory.setItem(9, item);
        inventory.setItem(17, item);
        inventory.setItem(36, item);
        inventory.setItem(44, item);
        inventory.setItem(45, item);
        inventory.setItem(46, item);
        inventory.setItem(48, item);
        inventory.setItem(49, item);
        inventory.setItem(50, item);
        inventory.setItem(52, item);
        inventory.setItem(53, item);

        inventory.setItem(10, GUIHandler.INSTANCE.createItemStack(Material.REDSTONE, "§cRed Dust", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.red-dust", 30) + " souls", "§7Click to buy")));

        item = GUIHandler.INSTANCE.createItemStack(Material.WOOL, "§cAngry", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.angry", 30) + " souls", "§7Click to buy"));

        item.setDurability((short) 14);

        inventory.setItem(12, item);

        inventory.setItem(13, GUIHandler.INSTANCE.createItemStack(Material.COAL, "§8Smoke", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.smoke", 30) + " souls", "§7Click to buy")));
        inventory.setItem(14, GUIHandler.INSTANCE.createItemStack(Material.RED_ROSE, "§cHeart", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.heart", 30) + " souls", "§7Click to buy")));
        inventory.setItem(16, GUIHandler.INSTANCE.createItemStack(Material.FIREWORK, "§cFire§fwork", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.firework", 30) + " souls", "§7Click to buy")));

        inventory.setItem(19, GUIHandler.INSTANCE.createItemStack(Material.LAVA_BUCKET, "§6Lava", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.lava", 60) + " souls", "§7Click to buy")));
        inventory.setItem(21, GUIHandler.INSTANCE.createItemStack(Material.BLAZE_POWDER, "§6Flame", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.flame", 60) + " souls", "§7Click to buy")));
        inventory.setItem(23, GUIHandler.INSTANCE.createItemStack(Material.GHAST_TEAR, "§fSnow", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.snow", 60) + " souls", "§7Click to buy")));
        inventory.setItem(25, GUIHandler.INSTANCE.createItemStack(Material.SNOW_BALL, "§fSnowball", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.snowball", 60) + " souls", "§7Click to buy")));

        inventory.setItem(28, GUIHandler.INSTANCE.createItemStack(Material.WEB, "§fCloud", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.cloud", 120) + " souls", "§7Click to buy")));

        item = GUIHandler.INSTANCE.createItemStack(Material.WOOL, "§aHappy", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.happy", 120) + " souls", "§7Click to buy"));

        item.setDurability((short) 5);

        inventory.setItem(30, item);

        inventory.setItem(32, GUIHandler.INSTANCE.createItemStack(Material.SLIME_BALL, "§aSlime", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.slime", 120) + " souls", "§7Click to buy")));
        inventory.setItem(34, GUIHandler.INSTANCE.createItemStack(Material.WATER_BUCKET, "§1Water", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.water", 120) + " souls", "§7Click to buy")));

        inventory.setItem(37, GUIHandler.INSTANCE.createItemStack(Material.ENDER_CHEST, "§8Town Aura", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.town-aura", 150) + " souls", "§7Click to buy")));
        inventory.setItem(39, GUIHandler.INSTANCE.createItemStack(Material.ENCHANTMENT_TABLE, "§bEnchant", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.enchant", 150) + " souls", "§7Click to buy")));
        inventory.setItem(40, GUIHandler.INSTANCE.createItemStack(Material.ICE, "§bIce", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.ice", 150) + " souls", "§7Click to buy")));
        inventory.setItem(41, GUIHandler.INSTANCE.createItemStack(Material.ENDER_PORTAL_FRAME, "§5Portal", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.portal", 150) + " souls", "§7Click to buy")));
        inventory.setItem(43, GUIHandler.INSTANCE.createItemStack(Material.NOTE_BLOCK, "§3Note", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.note", 150) + " souls", "§7Click to buy")));
        inventory.setItem(47, GUIHandler.INSTANCE.createItemStack(Material.BREWING_STAND_ITEM, "§3Magic", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.magic", 210) + " souls", "§7Click to buy")));
        inventory.setItem(49, GUIHandler.INSTANCE.createItemStack(Material.NETHER_STAR, "§fSpell", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.spell", 210) + " souls", "§7Click to buy")));

        item = GUIHandler.INSTANCE.createItemStack(Material.POTION, "§9Bubbles", Arrays.asList("§7Price: §6" + trails.getInt("trails-prices.bubbles", 210) + " souls", "§7Click to buy"));

        item.setDurability((short) 0);

        inventory.setItem(51, item);

        projectiles = GUIHandler.INSTANCE.createGUI(inventory, event -> {
            final ItemStack itemStack = event.getClickedItem();

            if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getType() == Material.STAINED_GLASS_PANE || itemStack.getType() == Material.ARROW) return;

            final int slot = event.getSlot();

            final Player player = event.getPlayer();

            final UUID uuid = player.getUniqueId();

            final int activeTrail = ActiveTrail.INSTANCE.get(uuid);
            final List<Integer> projectileTrails = Trails.INSTANCE.get(uuid);

            final String s = itemStack.getItemMeta().getDisplayName();

            if (activeTrail == slot) {
                ActiveTrail.INSTANCE.set(uuid, 0);
                MessageHandler.INSTANCE.sendMessage(player, "trails.un-equip", new String[]{"trail," + s});

                openGUI(player);
                return;
            }

            if (projectileTrails.contains(slot)) {
                ActiveTrail.INSTANCE.set(uuid, slot);
                MessageHandler.INSTANCE.sendMessage(player, "trails.equip", new String[]{"trail," + s});

                openGUI(player);
                return;
            }


            System.out.println(Souls.INSTANCE.get(uuid) + " - " + Integer.parseInt(ChatColor.stripColor(itemStack.getItemMeta().getLore().get(0).split(": ")[1].split(" ")[0].replace(",", ""))));

            if (Souls.INSTANCE.remove(uuid, Integer.parseInt(ChatColor.stripColor(itemStack.getItemMeta().getLore().get(0).split(": ")[1].split(" ")[0].replace(",", ""))))) {
                Trails.INSTANCE.add(uuid, slot);
                ActiveTrail.INSTANCE.set(uuid, slot);

                MessageHandler.INSTANCE.sendMessage(player, "trails.buy.success", new String[]{"trail," + s});

                openGUI(player);
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "trails.buy.failed", new String[]{"trail," + s});
        });
    }

    public void openGUI(final Player player) {
        if (!projectileClone.containsKey(player.getUniqueId())) {
            projectileClone.put(player.getUniqueId(), GUIHandler.INSTANCE.cloneGUI(projectiles));
        }

        updateArrowTrailGUI(player).openGUI(player);
    }

    private GUI updateArrowTrailGUI(final Player player) {
        final GUI gui = projectileClone.get(player.getUniqueId());
        final Inventory inventory = gui.getInventory();

        ItemMeta itemMeta;
        ItemStack itemStack;

        final int active = ActiveTrail.INSTANCE.get(player.getUniqueId());

        final List<Integer> trails = Trails.INSTANCE.get(player.getUniqueId());

        for (int i = 0; i < inventory.getSize(); i++) {
            itemStack = inventory.getItem(i);

            if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.getType() != Material.STAINED_GLASS_PANE) {
                if (itemStack.getType() == Material.ARROW) {
                    itemMeta = itemStack.getItemMeta();

                    if (active != 0) {
                        itemMeta.setLore(Collections.singletonList("§7Currently set to " + inventory.getItem(active).getItemMeta().getDisplayName()));
                        itemStack.setItemMeta(itemMeta);
                        continue;
                    }

                    itemMeta.setLore(Collections.singletonList("§7Currently set to default"));
                    itemStack.setItemMeta(itemMeta);
                    continue;
                }

                if (trails.contains(i)) {
                    itemMeta = itemStack.getItemMeta();

                    itemMeta.setLore(Collections.singletonList(active != i ? "§7Click to equip" : "§7Click to unequip"));

                    itemStack.setItemMeta(itemMeta);
                }
            }
        }

        gui.setInventory(inventory);

        return gui;
    }

    public Configuration getTrails() {
        return trails;
    }

    public boolean playParticle(final UUID uuid, final Projectile projectile) {
        final int i = ActiveTrail.INSTANCE.get(uuid);

        ParticleEffect effect = null;

        switch (i) {
            case 10:
                effect = ParticleEffect.REDSTONE;
                break;
            case 12:
                effect = ParticleEffect.VILLAGER_ANGRY;
                break;
            case 13:
                effect = ParticleEffect.SMOKE_NORMAL;
                break;
            case 14:
                effect = ParticleEffect.HEART;
                break;
            case 16:
                effect = ParticleEffect.FIREWORKS_SPARK;
                break;
            case 19:
                effect = ParticleEffect.LAVA;
                break;
            case 21:
                effect = ParticleEffect.FLAME;
                break;
            case 23:
                effect = ParticleEffect.SNOW_SHOVEL;
                break;
            case 25:
                effect = ParticleEffect.SNOWBALL;
                break;
            case 28:
                effect = ParticleEffect.CLOUD;
                break;
            case 30:
                effect = ParticleEffect.VILLAGER_HAPPY;
                break;
            case 32:
                effect = ParticleEffect.SLIME;
                break;
            case 34:
                effect = ParticleEffect.WATER_SPLASH;
                break;
            case 37:
                effect = ParticleEffect.TOWN_AURA;
                break;
            case 39:
                effect = ParticleEffect.ENCHANTMENT_TABLE;
                break;
            case 40:
                effect = ParticleEffect.BLOCK_CRACK;
                break;
            case 41:
                effect = ParticleEffect.PORTAL;
                break;
            case 43:
                effect = ParticleEffect.NOTE;
                break;
            case 47:
                effect = ParticleEffect.CRIT_MAGIC;
                break;
            case 49:
                effect = ParticleEffect.SPELL_WITCH;
                break;
            case 51:
                effect = ParticleEffect.WATER_BUBBLE;
                break;
        }

        if (effect == null) return false;

        final ParticleBuilder builder = new ParticleBuilder(effect, projectile.getLocation().clone().add(0, -1, 0));

        if (effect == ParticleEffect.BLOCK_CRACK) {
            builder.setParticleData(new BlockTexture(Material.ICE));
        }

        final int[] c = {1};

        TaskHandler.INSTANCE.runTaskTimerAsync(new BukkitRunnable() {
            @Override
            public void run() {
                if (!projectile.isDead()) {

                    if (builder.getParticle() == ParticleEffect.NOTE) {
                        if (c[0] == 26) c[0] = 1;

                        builder.setParticleData(new NoteColor(c[0]));

                        c[0]++;
                    }

                    builder.setLocation(projectile.getLocation());

                    builder.display();
                    return;
                }

                cancel();
            }
        }, 1);

        return true;
    }
}
