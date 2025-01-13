package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import pebbleprojects.strongMCPvP.databaseData.*;
import pebbleprojects.strongMCPvP.functions.GUI;
import pebbleprojects.strongMCPvP.functions.quests.PlayerQuests;
import pebbleprojects.strongMCPvP.functions.quests.Quest;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestsHandler {

    private final GUI questsGUI;
    private final Random random;
    private Configuration quests;
    private final File questsFile;
    public static QuestsHandler INSTANCE;
    private final Map<Integer, Quest> questsList;
    private final Map<UUID, GUI> questsGUIClones;

    public QuestsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Quests Handler...");

        INSTANCE = this;

        random = new Random();

        questsList = new ConcurrentHashMap<>();
        questsGUIClones = new ConcurrentHashMap<>();

        questsFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "quests.yml");

        final Inventory inventory = Bukkit.createInventory(null, 27, "Player Quests");

        questsGUI = GUIHandler.INSTANCE.createGUI(inventory, event -> {});

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Quests Handler!");
    }

    public void update() {
        questsList.clear();

        if (!questsFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("quests.yml", questsFile);
        }

        try {
            quests = ConfigurationProvider.getProvider(YamlConfiguration.class).load(questsFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load quests.yml in memory: " + e.getMessage());
        }

        final File questsDirectory = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "quests");

        if (questsDirectory.mkdir()) {
            DataHandler.INSTANCE.copyToPluginDirectory("quests/kills.yml", new File(questsDirectory, "kills.yml"));
        }

        final File[] quests = questsDirectory.listFiles();
        if (quests == null) return;

        for (int i = 0; i < quests.length; i++) {
            try {
                questsList.put(i, new Quest(ConfigurationProvider.getProvider(YamlConfiguration.class).load(quests[i])));
            } catch (final IOException e) {
                DataHandler.INSTANCE.getLogger().warning("Unable to load quest " + quests[i].getName() + " in memory: " + e.getMessage());
            }
        }
    }

    public void openGUI(final Player player) {
        if (!questsGUIClones.containsKey(player.getUniqueId()))
            questsGUIClones.put(player.getUniqueId(), GUIHandler.INSTANCE.cloneGUI(questsGUI));

        updateQuestsGUI(player).openGUI(player);
    }

    private GUI updateQuestsGUI(final Player player) {
        final UUID uuid = player.getUniqueId();

        final GUI gui = questsGUIClones.get(uuid);
        final Inventory inventory = gui.getInventory();

        int i = 11;
        final PlayerQuests playerQuests = Quests.INSTANCE.get(uuid);
        for (final int questId : playerQuests.getQuestsAsSet()) {
            final Quest quest = getQuest(questId);
            if (quest != null && !quest.hasFinished(player)) {
                inventory.setItem(i, GUIHandler.INSTANCE.createItemStack(Material.PAPER, "§7Quest #" + getCounter(i), Collections.singletonList(quest.getDescription()), ItemFlag.HIDE_ENCHANTS));
            } else {
                inventory.setItem(i, GUIHandler.INSTANCE.createItemStack(Material.PAPER, "§7Quest #" + getCounter(i), Collections.singletonList("§7Come back later!"), ItemFlag.HIDE_ENCHANTS));
            }

            i += 2;
        }

        while (i <= 15) {
            inventory.setItem(i, GUIHandler.INSTANCE.createItemStack(Material.PAPER, "§7Quest #" + getCounter(i), Collections.singletonList("§7Come back later!"), ItemFlag.HIDE_ENCHANTS));

            i += 2;
        }

        gui.setInventory(inventory);

        return gui;
    }

    public void setRandomQuests(final Player player) {
        final long now = System.currentTimeMillis();
        final PlayerQuests playerQuests = Quests.INSTANCE.get(player.getUniqueId());

        while (playerQuests.getQuestsAsSet().size() < Math.min(questsList.size(), 3)) {
            final int randomQuestId = random.nextInt(questsList.size());
            if (!playerQuests.getQuestsAsSet().contains(randomQuestId))
                playerQuests.addQuest(player, randomQuestId, now, now + questsList.get(randomQuestId).getEvery() * 1000L);
        }
    }

    public Configuration getQuests() {
        return quests;
    }

    public Quest getQuest(final int questId) {
        return questsList.get(questId);
    }

    public void onKill(final Player player) {
        final PlayerQuests playerQuests = Quests.INSTANCE.get(player.getUniqueId());
        if (playerQuests != null) playerQuests.callEvent(player, "kill");
    }

    public void onDeath(final Player player) {
        final PlayerQuests playerQuests = Quests.INSTANCE.get(player.getUniqueId());
        if (playerQuests != null) playerQuests.callEvent(player, "death");
    }

    private int getCounter(final int i) {
        switch (i) {
            case 11:
                return 1;
            case 13:
                return 2;
            case 15:
                return 3;
            default:
                return -1;
        }
    }
}
