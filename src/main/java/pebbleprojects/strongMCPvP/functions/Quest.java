package pebbleprojects.strongMCPvP.functions;

import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.functions.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Quest {

    private final String name, description;
    private final Map<UUID, Integer> counters;
    private final int souls, points, rewardCounter, initialCounter;
    private final List<String> resetEvents, incrementEvents, decrementEvents;

    public Quest(final Configuration config) {
        name = config.getString("name");
        description = config.getString("description");

        counters = new ConcurrentHashMap<>();

        resetEvents = config.getStringList("events.reset-counter");
        incrementEvents = config.getStringList("events.increment-counter");
        decrementEvents = config.getStringList("events.decrement-counter");

        souls = config.getInt("rewards.souls");
        points = config.getInt("rewards.points");
        rewardCounter = config.getInt("reward-counter");
        initialCounter = config.getInt("initial-counter");
    }

    public void callEvent(final Player player, final String event) {
        if (resetEvents.contains(event)) {
            resetCounter(player);
            return;
        }

        if (incrementEvents.contains(event)) {
            incrementCounter(player);
            return;
        }

        if (decrementEvents.contains(event))
            decrementCounter(player);
    }

    private void resetCounter(final Player player) {
        counters.remove(player.getUniqueId());
    }

    private void incrementCounter(final Player player) {
        counters.put(player.getUniqueId(), counters.getOrDefault(player.getUniqueId(), 0) + 1);
    }

    private void decrementCounter(final Player player) {
        counters.put(player.getUniqueId(), Math.max(counters.getOrDefault(player.getUniqueId(), 0) - 1, 0));
    }
}
