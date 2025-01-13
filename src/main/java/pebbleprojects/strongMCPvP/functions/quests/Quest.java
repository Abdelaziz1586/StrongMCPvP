package pebbleprojects.strongMCPvP.functions.quests;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.databaseData.Points;
import pebbleprojects.strongMCPvP.databaseData.QuestCompletion;
import pebbleprojects.strongMCPvP.databaseData.Souls;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.MessageHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Quest {

    private final String name, description;
    private final Map<UUID, Integer> counters;
    private final int every, souls, points, rewardCounter, initialCounter;
    private final List<String> resetEvents, incrementEvents, decrementEvents;

    public Quest(final Configuration config) {
        name = ChatColor.translateAlternateColorCodes('&', config.getString("name", ""));
        description = ChatColor.translateAlternateColorCodes('&', config.getString("description", ""));

        counters = new ConcurrentHashMap<>();

        resetEvents = config.getStringList("events.reset-counter");
        incrementEvents = config.getStringList("events.increment-counter");
        decrementEvents = config.getStringList("events.decrement-counter");

        every = config.getInt("every", 86400);

        souls = config.getInt("rewards.souls");
        points = config.getInt("rewards.points");
        rewardCounter = config.getInt("reward-counter");
        initialCounter = config.getInt("initial-counter");
    }

    public void callEvent(final Player player, final String event) {
        if (resetEvents.contains(event)) {
            resetCounter(player.getUniqueId());
            return;
        }

        if (incrementEvents.contains(event)) {
            incrementCounter(player);
            return;
        }

        if (decrementEvents.contains(event))
            decrementCounter(player);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getEvery() {
        return every;
    }

    public boolean hasFinished(final Player player) {
        return counters.getOrDefault(player.getUniqueId(), initialCounter) >= rewardCounter;
    }

    public void setCounter(final Player player, final int counter) {
        counters.put(player.getUniqueId(), counter);

        checkAndReward(player, false);
    }

    public int getCounter(final UUID uuid) {
        return counters.getOrDefault(uuid, 0);
    }

    public void resetCounter(final UUID uuid) {
        counters.remove(uuid);
    }

    private void incrementCounter(final Player player) {
        counters.put(player.getUniqueId(), counters.getOrDefault(player.getUniqueId(), initialCounter) + 1);

        checkAndReward(player, true);
    }

    private void decrementCounter(final Player player) {
        counters.put(player.getUniqueId(), Math.max(counters.getOrDefault(player.getUniqueId(), initialCounter) - 1, 0));
    }

    private void checkAndReward(final Player player, final boolean increment) {
        final UUID uuid = player.getUniqueId();
        if (increment && counters.getOrDefault(uuid, initialCounter) == rewardCounter) {
            Souls.INSTANCE.add(uuid, souls);
            Points.INSTANCE.add(uuid, points);

            if (QuestCompletion.INSTANCE.get(uuid))
                MessageHandler.INSTANCE.sendMessage(player, "quests.completion", new String[]{"quest," + name, "description," + description, "souls," + souls, "points," + points});
        }
    }
}
