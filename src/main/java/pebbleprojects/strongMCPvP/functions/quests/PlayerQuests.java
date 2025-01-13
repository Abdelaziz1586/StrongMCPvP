package pebbleprojects.strongMCPvP.functions.quests;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pebbleprojects.strongMCPvP.databaseData.QuestReminder;
import pebbleprojects.strongMCPvP.handlers.MessageHandler;
import pebbleprojects.strongMCPvP.handlers.QuestsHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class PlayerQuests {

    private final UUID uuid;
    private final Map<Integer, Long> quests;
    private final Map<Integer, BukkitTask> tasks;

    public PlayerQuests(final UUID uuid) {
        this.uuid = uuid;

        tasks = new ConcurrentHashMap<>();
        quests = new ConcurrentHashMap<>();
    }

    public void resetQuests() {
        quests.clear();

        for (final BukkitTask task : tasks.values()) {
            task.cancel();
        }

        tasks.clear();
    }

    public void addQuest(final Player player, final int questId, final long now, final long endTime) {
        if (quests.containsKey(questId)) return;

        quests.put(questId, endTime);

        final AtomicReference<Quest> quest = new AtomicReference<>(QuestsHandler.INSTANCE.getQuest(questId));
        if (quest.get().hasFinished(player)) return;

        if (QuestReminder.INSTANCE.get(player.getUniqueId()))
            MessageHandler.INSTANCE.sendMessage(player, "quests.reminder", new String[]{"quest," + quest.get().getName(), "description," + quest.get().getDescription()});

        tasks.put(questId, TaskHandler.INSTANCE.runLaterAsync(() -> {
            quest.set(QuestsHandler.INSTANCE.getQuest(questId));
            if (quest.get() != null) quest.get().resetCounter(uuid);

            tasks.remove(questId);
            quests.remove(questId);

            QuestsHandler.INSTANCE.setRandomQuests(player);
        }, (endTime - now) / 1000 * 20));
    }

    public void removeQuest(final int questId) {
        if (tasks.containsKey(questId)) {
            tasks.get(questId).cancel();
            tasks.remove(questId);
        }

        quests.remove(questId);

        final Quest quest = QuestsHandler.INSTANCE.getQuest(questId);
        if (quest != null) quest.resetCounter(uuid);
    }

    public void callEvent(final Player player, final String event) {
        for (final int questId : quests.keySet()) {
            final Quest quest = QuestsHandler.INSTANCE.getQuest(questId);
            if (quest == null) continue;

            quest.callEvent(player, event);
        }
    }

    public List<String> getQuests() {
        return quests.entrySet().stream()
                .map(entry -> {
                    final Quest quest = QuestsHandler.INSTANCE.getQuest(entry.getKey());

                    return quest == null ? null : entry.getKey() + "," + entry.getValue() + "," + quest.getCounter(uuid);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Set<Integer> getQuestsAsSet() {
        return quests.keySet();
    }
}