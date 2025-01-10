package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import pebbleprojects.strongMCPvP.functions.GUI;
import pebbleprojects.strongMCPvP.functions.NPC;
import pebbleprojects.strongMCPvP.functions.Vector3D;
import pebbleprojects.strongMCPvP.functions.config.Configuration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NPCHandler {

    private final List<GUI> guis;
    public static NPCHandler INSTANCE;
    private final ItemStack nextPage, previousPage;
    private final Map<UUID, Map<Integer, GUI>> createGUIs;
    private final Map<Integer, NPC> shopNPCs, perksNPCs, trailsNPC, settingsNPC;

    public NPCHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading NPC Handler...");

        INSTANCE = this;

        guis = new ArrayList<>();

        shopNPCs = new ConcurrentHashMap<>();
        perksNPCs = new ConcurrentHashMap<>();
        trailsNPC = new ConcurrentHashMap<>();
        settingsNPC = new ConcurrentHashMap<>();

        nextPage = getHead("MHF_ArrowRight", "§7Next Page", "§7Left Click: Next Page");
        previousPage = getHead("MHF_ArrowLeft", "§7Previous Page", "§7Left Click: Go Back");

        createGUIs = new ConcurrentHashMap<>();

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded NPC Handler!");
    }

    public void update() {
        TaskHandler.INSTANCE.runAsync(() -> {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                for (final NPC npc : shopNPCs.values()) {
                    npc.sendRemovePacket(player);
                }

                for (final NPC npc : perksNPCs.values()) {
                    npc.sendRemovePacket(player);
                }

                for (final NPC npc : trailsNPC.values()) {
                    npc.sendRemovePacket(player);
                }
            }

            shopNPCs.clear();
            perksNPCs.clear();
            trailsNPC.clear();
            settingsNPC.clear();

            final Configuration mainSection = DataHandler.INSTANCE.getData().getSection("NPCs");

            if (mainSection != null) {
                Configuration section = mainSection.getSection("shop");

                if (section != null) {
                    for (final String id : section.getKeys()) {
                        try {
                            final int i = Integer.parseInt(id);
                            final NPC npc = toNPC(i, 0, section.getSection(id));

                            if (npc == null) continue;

                            shopNPCs.put(i, npc);
                        } catch (final NumberFormatException ignored) {
                        }
                    }
                }

                section = mainSection.getSection("perks");

                if (section != null) {
                    for (final String id : section.getKeys()) {
                        try {
                            final int i = Integer.parseInt(id);
                            final NPC npc = toNPC(i, 1, section.getSection(id));

                            if (npc == null) continue;

                            perksNPCs.put(i, npc);
                        } catch (final NumberFormatException ignored) {
                        }
                    }
                }

                section = mainSection.getSection("trails");

                if (section != null) {
                    for (final String id : section.getKeys()) {
                        try {
                            final int i = Integer.parseInt(id);
                            final NPC npc = toNPC(i, 2, section.getSection(id));

                            if (npc == null) continue;

                            trailsNPC.put(i, npc);
                        } catch (final NumberFormatException ignored) {
                        }
                    }
                }

                section = mainSection.getSection("settings");

                if (section != null) {
                    for (final String id : section.getKeys()) {
                        try {
                            final int i = Integer.parseInt(id);
                            final NPC npc = toNPC(i, 3, section.getSection(id));

                            if (npc == null) continue;

                            settingsNPC.put(i, npc);
                        } catch (final NumberFormatException ignored) {
                        }
                    }
                }
            }

            for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
                for (final NPC npc : shopNPCs.values()) {
                    npc.sendShowPacket(player);
                }

                for (final NPC npc : perksNPCs.values()) {
                    npc.sendShowPacket(player);
                }

                for (final NPC npc : trailsNPC.values()) {
                    npc.sendShowPacket(player);
                }

                for (final NPC npc : settingsNPC.values()) {
                    npc.sendShowPacket(player);
                }
            }

            prepareGUI();
        });
    }

    public void shutdown() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            for (final NPC npc : shopNPCs.values()) {
                npc.sendRemovePacket(player);
            }

            for (final NPC npc : perksNPCs.values()) {
                npc.sendRemovePacket(player);
            }

            for (final NPC npc : trailsNPC.values()) {
                npc.sendRemovePacket(player);
            }

            for (final NPC npc : settingsNPC.values()) {
                npc.sendRemovePacket(player);
            }
        }
    }

    public void openGUI(final Player player) {
        if (guis.isEmpty()) return;

        guis.get(0).openGUI(player);
    }

    public List<NPC> getNPCs() {
        return Stream.concat(
                        Stream.concat(shopNPCs.values().stream(), perksNPCs.values().stream()),
                        Stream.concat(trailsNPC.values().stream(), settingsNPC.values().stream())
                )
                .collect(Collectors.toList());
    }


    public void createNPC(final Location location, final String name, String skinName, final int type) {
        NPC npc = null;

        switch (type) {
            case 0:
                npc = new NPC(shopNPCs.size() + 1, type, ChatColor.translateAlternateColorCodes('&', name != null ? name : ShopHandler.INSTANCE.getShop().getString("npc.defaultName", "&e&lShop")), null, location);

                if (skinName == null) skinName = ShopHandler.INSTANCE.getShop().getString("npc.defaultSkinName", "HoneySalted");

                shopNPCs.put(shopNPCs.size() + 1, npc);
                break;
            case 1:
                npc = new NPC(perksNPCs.size() + 1, type, ChatColor.translateAlternateColorCodes('&', name != null ? name : PerksHandler.INSTANCE.getPerks().getString("npc.defaultName", "&d&lPerks")), null, location);

                if (skinName == null) skinName = PerksHandler.INSTANCE.getPerks().getString("npc.defaultSkinName", "HoneySalted");

                perksNPCs.put(perksNPCs.size() + 1, npc);
                break;
            case 2:
                npc = new NPC(trailsNPC.size() + 1, type, ChatColor.translateAlternateColorCodes('&', name != null ? name : TrailsHandler.INSTANCE.getTrails().getString("npc.defaultName", "&6&lTrails")), null, location);

                if (skinName == null) skinName = TrailsHandler.INSTANCE.getTrails().getString("npc.defaultSkinName", "HoneySalted");

                trailsNPC.put(trailsNPC.size() + 1, npc);
                break;
            case 3:
                npc = new NPC(settingsNPC.size() + 1, type, ChatColor.translateAlternateColorCodes('&', name != null ? name : SettingsHandler.INSTANCE.getSettings().getString("npc.defaultName", "&5&lSettings")), null, location);

                if (skinName == null) skinName = SettingsHandler.INSTANCE.getSettings().getString("npc.defaultSkinName", "HoneySalted");

                settingsNPC.put(settingsNPC.size() + 1, npc);
                break;
        }

        if (npc == null) return;

        npc.setSkin(skinName);

        addNPC(npc);

        saveNPCs();
    }

    public boolean removeShopNPC(final int id) {
        if (shopNPCs.containsKey(id)) {
            removeNPC(shopNPCs.get(id));
            shopNPCs.remove(id);

            saveNPCs();
            return true;
        }

        return false;
    }

    public boolean removePerksNPC(final int id) {
        if (perksNPCs.containsKey(id)) {
            removeNPC(perksNPCs.get(id));
            perksNPCs.remove(id);

            saveNPCs();
            return true;
        }

        return false;
    }

    public boolean removeTrailsNPC(final int id) {
        if (trailsNPC.containsKey(id)) {
            removeNPC(trailsNPC.get(id));
            trailsNPC.remove(id);

            saveNPCs();
            return true;
        }

        return false;
    }

    public boolean removeSettingsNPC(final int id) {
        if (settingsNPC.containsKey(id)) {
            removeNPC(settingsNPC.get(id));
            settingsNPC.remove(id);

            saveNPCs();
            return true;
        }

        return false;
    }

    public void removeNPC(final Player player) {
        final NPC target = getTargetNPC(player);

        if (target == null) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.others.not-looking-at-npc", null);
            return;
        }

        switch (target.getType()) {
            case 0:
                removeShopNPC(target.getId());
                MessageHandler.INSTANCE.sendMessage(player, "npc.shop.remove.success", new String[]{"id," + target.getId()});
                break;
            case 1:
                removePerksNPC(target.getId());
                MessageHandler.INSTANCE.sendMessage(player, "npc.perks.remove.success", new String[]{"id," + target.getId()});
                break;
            case 2:
                removeTrailsNPC(target.getId());
                MessageHandler.INSTANCE.sendMessage(player, "npc.trails.remove.success", new String[]{"id," + target.getId()});
                break;
            case 3:
                removeSettingsNPC(target.getId());
                MessageHandler.INSTANCE.sendMessage(player, "npc.settings.remove.success", new String[]{"id," + target.getId()});
                break;
        }
    }

    public void loadNPCs(final Player player) {
        TaskHandler.INSTANCE.runAsync(() -> {
            for (final NPC npc : shopNPCs.values()) {
                npc.sendRemovePacket(player);
                npc.sendShowPacket(player);
            }

            for (final NPC npc : perksNPCs.values()) {
                npc.sendRemovePacket(player);
                npc.sendShowPacket(player);
            }

            for (final NPC npc : trailsNPC.values()) {
                npc.sendRemovePacket(player);
                npc.sendShowPacket(player);
            }

            for (final NPC npc : settingsNPC.values()) {
                npc.sendRemovePacket(player);
                npc.sendShowPacket(player);
            }
        });
    }

    public void unloadNPCs(final Player player) {
        TaskHandler.INSTANCE.runAsync(() -> {
            for (final NPC npc : shopNPCs.values()) {
                npc.sendRemovePacket(player);
            }

            for (final NPC npc : perksNPCs.values()) {
                npc.sendRemovePacket(player);
            }

            for (final NPC npc : trailsNPC.values()) {
                npc.sendRemovePacket(player);
            }

            for (final NPC npc : settingsNPC.values()) {
                npc.sendRemovePacket(player);
            }
        });
    }

    private void saveNPCs() {
        DataHandler.INSTANCE.getData().set("NPCs", allToSection());
        DataHandler.INSTANCE.saveData();

        prepareGUI();
    }

    private NPC toNPC(final int id, final int type, final Configuration section) {
        if (section == null || !section.contains("name") || !section.contains("skinName") || !section.contains("location") || !section.contains("textures") || !section.contains("signature")) return null;

        final Location location = LocationHandler.INSTANCE.convertToLocation(section.getString("location"));

        if (location == null) return null;

        final NPC npc = new NPC(id, type, section.getString("name"), section.getString("skinName"), location);

        npc.setTexturesAndSignature(section.getString("textures"), section.getString("signature"));

        return npc;
    }

    private Configuration allToSection() {
        final Configuration mainSection = new Configuration(),
                shopSection = new Configuration(),
                perksSection = new Configuration(),
                trailsSection = new Configuration(),
                settingsSection = new Configuration();

        for (final Map.Entry<Integer, NPC> entry : shopNPCs.entrySet()) {
            shopSection.set(String.valueOf(entry.getKey()), toSection(entry.getValue()));
        }

        for (final Map.Entry<Integer, NPC> entry : perksNPCs.entrySet()) {
            perksSection.set(String.valueOf(entry.getKey()), toSection(entry.getValue()));
        }

        for (final Map.Entry<Integer, NPC> entry : trailsNPC.entrySet()) {
            trailsSection.set(String.valueOf(entry.getKey()), toSection(entry.getValue()));
        }

        for (final Map.Entry<Integer, NPC> entry : settingsNPC.entrySet()) {
            settingsSection.set(String.valueOf(entry.getKey()), toSection(entry.getValue()));
        }

        mainSection.set("shop", shopSection);
        mainSection.set("perks", perksSection);
        mainSection.set("trails", trailsSection);
        mainSection.set("settings", settingsSection);

        return mainSection;
    }

    private Configuration toSection(final NPC npc) {
        if (npc == null) return null;

        final Configuration section = new Configuration();

        section.set("name", npc.getName());
        section.set("skinName", npc.getSkinName());
        section.set("textures", npc.getTextures());
        section.set("signature", npc.getSignature());
        section.set("location", LocationHandler.INSTANCE.convertToString(npc.getLocation()));

        return section;
    }

    private void removeNPC(final NPC npc) {
        for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
            npc.sendRemovePacket(player);
        }
    }

    private void addNPC(final NPC npc) {
        for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
            npc.sendShowPacket(player);
        }
    }

    private NPC getTargetNPC(final Player player) {
        NPC targetNPC = null;
        final Location playerPos = player.getEyeLocation();
        final Vector3D playerDir = new Vector3D(playerPos.getDirection()),
                playerStart = new Vector3D(playerPos),
                playerEnd = playerStart.add(playerDir.multiply(100));

        for (final NPC npc : getNPCs()) {
            final Vector3D targetPos = new Vector3D(npc.getLocation()),
                    minimum = targetPos.add(-0.5, 0, -0.5),
                    maximum = targetPos.add(0.5, 1.67, 0.5);

            if (hasIntersection(playerStart, playerEnd, minimum, maximum)) {
                if (targetNPC == null || targetNPC.getLocation().distanceSquared(playerPos) > npc.getLocation().distanceSquared(playerPos)) {
                    targetNPC = npc;
                }
            }
        }

        return targetNPC;
    }

    private boolean hasIntersection(final Vector3D p1, final Vector3D p2, final Vector3D min, final Vector3D max) {
        final double epsilon = 0.0001;
        final Vector3D d = p2.subtract(p1).multiply(0.5),
                e = max.subtract(min).multiply(0.5),
                c = p1.add(d).subtract(min.add(max).multiply(0.5)),
                ad = d.abs();

        final double absCx = Math.abs(c.x),
                absCy = Math.abs(c.y),
                absCz = Math.abs(c.z);

        return !(absCx > e.x + ad.x)
                && !(absCy > e.y + ad.y)
                && !(absCz > e.z + ad.z)
                && !(Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon) &&
                !(Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon)
                && !(Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon);
    }

    private void prepareGUI() {
        if (!guis.isEmpty()) guis.clear();

        final List<Inventory> inventories = new ArrayList<>();
        inventories.add(Bukkit.createInventory(null, 18, "§b§lNPC Manager"));

        final List<NPC> npcs = NPCHandler.INSTANCE.getNPCs();
        final ItemStack head = getHead("CONSOLE", "§bTotal NPC Count: §e" + npcs.size(), "§7Left Click: Create Shop NPC", "§7Right Click: Create Perks NPC", "§7Shift Click: Create Trails NPC", "§7Middle Click: Create Settings NPC");
        inventories.get(0).setItem(4, head);

        int i = 0;
        int currentInventory = 0;
        for (final NPC npc : npcs) {
            if (npc == null) {
                continue;
            }

            if (i >= 8) {
                i = 1;
                currentInventory++;
                if (currentInventory >= inventories.size()) {
                    inventories.add(Bukkit.createInventory(null, 18, "§b§lNPC Manager - Page " + (currentInventory + 1)));
                }

                final ItemStack[] items = getNextAndPreviousPage(currentInventory + 1);
                inventories.get(currentInventory - 1).setItem(17, items[0]);
                inventories.get(currentInventory).setItem(9, items[1]);
                inventories.get(currentInventory).setItem(4, head);
            }

            inventories.get(currentInventory).setItem(i + 9, getHead(npc.getSkinName(), (npc.getType() == 0 ? "§eShopNPC " : npc.getType() == 1 ? "§dPerksNPC " : npc.getType() == 2 ? "§6TrailsNPC " : "§5SettingsNPC ") + npc.getId() + ": §r" + npc.getName(), "§7Left Click: Teleport", "§7Right Click: Delete"));
            i++;
        }

        for (int i2 = 0; i2 < inventories.size(); i2++) {
            final int finalI = i2;
            guis.add(GUIHandler.INSTANCE.createGUI(inventories.get(finalI), event -> {
                if (event.getSlot() == 4) {
                    openCreationGUI(event.getPlayer(), event.isMiddleClick() ? 3 : event.isShiftClick() ? 2 : event.isRightClick() ? 1 : 0);
                    return;
                }

                final ItemStack itemStack = event.getClickedItem();

                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    if (event.getSlot() == 9 && itemStack.getItemMeta().getLore().size() == 1) {
                        guis.get(finalI - 1).openGUI(event.getPlayer());
                        return;
                    }

                    if (event.getSlot() == 17) {
                        if (guis.size() >= finalI + 2) {
                            guis.get(finalI + 1).openGUI(event.getPlayer());
                        }
                        return;
                    }

                    final String name = itemStack.getItemMeta().getDisplayName();
                    try {
                        final int type = name.startsWith("§e") ? 0 : name.startsWith("§d") ? 1 : name.startsWith("§6") ? 2 : 3,
                                id = Integer.parseInt(name.split(" ")[1].split(":")[0]);

                        final NPC npc;

                        switch (type) {
                            case 0:
                                npc = shopNPCs.get(id);
                                break;
                            case 1:
                                npc = perksNPCs.get(id);
                                break;
                            case 2:
                                npc = trailsNPC.get(id);
                                break;
                            case 3:
                                npc = settingsNPC.get(id);
                                break;
                            default:
                                npc = null;
                                break;
                        }

                        if (npc == null) return;

                        final Player player = event.getPlayer();

                        if (event.isLeftClick()) {
                            player.teleport(npc.getLocation());
                        } else if (event.isRightClick()) {
                            removeNPC(npc, player);
                        }

                        player.closeInventory();
                    } catch (final NumberFormatException ignored) {}
                }
            }));
        }
    }

    private ItemStack[] getNextAndPreviousPage(final int page) {
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

    private ItemStack getHead(final String player, final String name, final String... lore) {
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

    private void openCreationGUI(final Player player, final int type) {
        final Map<Integer, GUI> guis = createGUIs.getOrDefault(player.getUniqueId(), new ConcurrentHashMap<>());

        if (guis.containsKey(type)) {
            ChatInputHandler.INSTANCE.removeInputRequest(player);

            guis.get(type).openGUI(player);
            return;
        }

        final Inventory inventory = Bukkit.createInventory(null, InventoryType.WORKBENCH, "Create " + (type == 0 ? "Shop" : type == 1 ? "Perks" : "Trails") + " NPC");

        String skinName = null;

        switch (type) {
            case 0:
                skinName = ShopHandler.INSTANCE.getShop().getString("npc.defaultSkinName", "HoneySalted");
                break;
            case 1:
                skinName = PerksHandler.INSTANCE.getPerks().getString("npc.defaultSkinName", "HoneySalted");
                break;
            case 2:
                skinName = TrailsHandler.INSTANCE.getTrails().getString("npc.defaultSkinName", "HoneySalted");
                break;
            case 3:
                skinName = SettingsHandler.INSTANCE.getSettings().getString("npc.defaultSkinName", "HoneySalted");
                break;
        }

        String name = null;

        switch (type) {
            case 0:
                name = ShopHandler.INSTANCE.getShop().getString("npc.defaultName", "&e&lShop");
                break;
            case 1:
                name = PerksHandler.INSTANCE.getPerks().getString("npc.defaultName", "&d&lPerks");
                break;
            case 2:
                name = TrailsHandler.INSTANCE.getTrails().getString("npc.defaultName", "&6&lTrails");
                break;
            case 3:
                name = SettingsHandler.INSTANCE.getSettings().getString("npc.defaultName", "&5&lSettings");
                break;
        }

        inventory.setItem(5, getHead(skinName, ChatColor.translateAlternateColorCodes('&', name == null ? "" : name), "§7Left Click: Set Name", "§7Right Click: Set Skin"));

        ItemStack itemStack = new ItemStack(Material.EMERALD);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName("§aCreate NPC");
        itemStack.setItemMeta(itemMeta);

        inventory.setItem(0, itemStack);

        itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName("§7");

        itemStack.setItemMeta(itemMeta);

        for (int i = 1; i < 10; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, itemStack);
            }
        }

        final GUI gui = GUIHandler.INSTANCE.createGUI(inventory, event -> {
            final Player clickedPlayer = event.getPlayer();

            if (event.getSlot() == 5) {
                if (event.isLeftClick()) {
                    ChatInputHandler.INSTANCE.inputFromThen(clickedPlayer, chatEvent -> {
                        String message = chatEvent.getMessage();

                        if (message.length() > 16 || message.length() < 3) {
                            MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + (type == 0 ? "shop" : type == 1 ? "perks" : "trails") + ".create.failed.name.invalid-length", null);

                            guis.get(type).openGUI(clickedPlayer);
                            return;
                        }

                        final ItemStack head = inventory.getItem(5);
                        final ItemMeta headMeta = head.getItemMeta();

                        message = ChatColor.translateAlternateColorCodes('&', message);
                        headMeta.setDisplayName(message);

                        head.setItemMeta(headMeta);

                        inventory.setItem(5, head);

                        final GUI guiToOpen = guis.get(type);

                        guiToOpen.setInventory(inventory);

                        guiToOpen.openGUI(clickedPlayer);

                        MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + (type == 0 ? "shop" : type == 1 ? "perks" : "trails") + ".create.success.name", new String[]{"name," + message});
                    }, () -> {
                        MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + (type == 0 ? "shop" : type == 1 ? "perks" : "trails") + ".create.failed.name.timeout", null);
                        guis.remove(type);

                        this.createGUIs.put(clickedPlayer.getUniqueId(), guis);
                    });

                    MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + (type == 0 ? "shop" : type == 1 ? "perks" : "trails") + ".create.requests.name", null);
                    return;
                }

                if (event.isRightClick()) {
                    ChatInputHandler.INSTANCE.inputFromThen(clickedPlayer, chatEvent -> {
                        final String message = chatEvent.getMessage();

                        if (message.length() > 16 || message.length() < 3) {
                            MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + (type == 0 ? "shop" : type == 1 ? "perks" : "trails") + ".create.failed.skinName.invalid-length", null);

                            guis.get(type).openGUI(clickedPlayer);
                            return;
                        }

                        final ItemStack head = inventory.getItem(5);
                        final SkullMeta headMeta = (SkullMeta) head.getItemMeta();

                        headMeta.setOwner(message);

                        head.setItemMeta(headMeta);

                        inventory.setItem(5, head);

                        final GUI guiToOpen = guis.get(type);

                        guiToOpen.setInventory(inventory);

                        guiToOpen.openGUI(clickedPlayer);

                        MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + (type == 0 ? "shop" : type == 1 ? "perks" : "trails") + ".create.success.skinName", new String[]{"skinName," + message});
                    }, () -> {
                        MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + (type == 0 ? "shop" : type == 1 ? "perks" : "trails") + ".create.failed.skinName.timeout", null);
                        guis.remove(type);

                        this.createGUIs.put(clickedPlayer.getUniqueId(), guis);
                    });

                    MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + (type == 0 ? "shop" : type == 1 ? "perks" : "trails") + ".create.requests.skinName", null);
                }
                return;
            }

            if (event.getSlot() == 0) {
                final ItemStack head = inventory.getItem(5);
                final SkullMeta headMeta = (SkullMeta) head.getItemMeta();

                clickedPlayer.closeInventory();

                final String key = type == 0 ? "shop" : type == 1 ? "perks" : type == 2 ? "trails" : "settings";

                MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + key + ".create.success.creation.creating", null);

                createNPC(clickedPlayer.getLocation(), headMeta.getDisplayName(), headMeta.getOwner(), type);

                MessageHandler.INSTANCE.sendMessage(clickedPlayer, "npc." + key + ".create.success.creation.created", null);

                this.createGUIs.remove(clickedPlayer.getUniqueId());
            }
        });

        guis.put(type, gui);

        gui.openGUI(player);
    }

    private void removeNPC(final NPC npc, final Player player) {
        switch (npc.getType()) {
            case 0:
                removeShopNPC(npc.getId());
                MessageHandler.INSTANCE.sendMessage(player, "npc.shop.remove.success", new String[]{"id," + npc.getId()});
                break;
            case 1:
                removePerksNPC(npc.getId());
                MessageHandler.INSTANCE.sendMessage(player, "npc.perks.remove.success", new String[]{"id," + npc.getId()});
                break;
            case 2:
                removeTrailsNPC(npc.getId());
                MessageHandler.INSTANCE.sendMessage(player, "npc.trails.remove.success", new String[]{"id," + npc.getId()});
                break;
            case 3:
                removeSettingsNPC(npc.getId());
                MessageHandler.INSTANCE.sendMessage(player, "npc.settings.remove.success", new String[]{"id," + npc.getId()});
                break;
        }
    }
}