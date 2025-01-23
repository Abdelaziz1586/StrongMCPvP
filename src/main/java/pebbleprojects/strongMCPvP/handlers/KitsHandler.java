package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.functions.Kit;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class KitsHandler {

    private final List<Kit> kits;
    public static KitsHandler INSTANCE;
    private final Map<UUID, Kit> playerKits;

    public KitsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Kits Handler...");

        INSTANCE = this;

        kits = new LinkedList<>();
        playerKits = new ConcurrentHashMap<>();

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Kits Handler!");
    }

    public void update() {
        kits.clear();

        final File kitsDirectory = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "kits");

        if (kitsDirectory.mkdir()) {
            DataHandler.INSTANCE.copyToPluginDirectory("kits/example.yml", new File(kitsDirectory, "GoldenHead.yml"));
        }

        final File[] kits = kitsDirectory.listFiles();

        if (kits == null) return;

        Configuration config;
        for (final File file : kits) {
            try {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

                this.kits.add(new Kit(config));

                DataHandler.INSTANCE.getLogger().info("Loaded kit " + file.getName());
            } catch (final IOException e) {
                DataHandler.INSTANCE.getLogger().warning("Unable to load kit " + file.getName() + " in memory: " + e.getMessage());
            }
        }

        sortPriorities();
    }

    public void openGUI(final Player player) {

    }

    public void applyKit(final Player player) {
        for (final Kit kit : kits) {
            if (kit.getPermission() != null && !kit.getPermission().isEmpty() && !player.hasPermission(kit.getPermission()))
                continue;

            kit.applyKit(player, false);
            playerKits.put(player.getUniqueId(), kit);
            return;
        }
    }

    public void kill(final Player player) {
        if (playerKits.containsKey(player.getUniqueId()))
            playerKits.get(player.getUniqueId()).applyKit(player, true);
    }

    private void sortPriorities() {
        final Map<Integer, Kit> priorities = new ConcurrentHashMap<>();

        for (final Kit kit : kits)
            priorities.put(kit.getPriority(), kit);

        kits.clear();

        final List<Kit> leastPriority = new ArrayList<>();
        final List<Integer> sortedPriorities = new LinkedList<>(priorities.keySet());
        Collections.sort(sortedPriorities);

        for (final int priority : sortedPriorities) {
            if (priority < 1) {
                leastPriority.add(priorities.get(priority));
                continue;
            }

            kits.add(priorities.get(priority));
        }

        kits.addAll(leastPriority);
    }

}
