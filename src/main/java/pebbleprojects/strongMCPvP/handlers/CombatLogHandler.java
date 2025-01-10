package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatLogHandler {

    private int combatLogDuration;
    public static CombatLogHandler INSTNACE;
    private final Map<UUID, BukkitTask> combatLog;

    public CombatLogHandler() {
        INSTNACE = this;

        combatLog = new ConcurrentHashMap<>();

        update();
    }

    public void update() {
        combatLogDuration = DataHandler.INSTANCE.getConfig().getInt("gameplay.combat-log", 10);
    }

    public void addToCombatLog(final Player player) {
        final UUID uuid = player.getUniqueId();

        if (!combatLog.containsKey(uuid))
            MessageHandler.INSTANCE.sendMessage(player, "gameplay.combat-log.start", new String[]{"combatLog," + combatLogDuration});
        else
            Optional.ofNullable(combatLog.remove(uuid)).ifPresent(BukkitTask::cancel);

        combatLog.put(uuid, TaskHandler.INSTANCE.runLaterAsync(() -> {
            combatLog.remove(uuid);
            MessageHandler.INSTANCE.sendMessage(player, "gameplay.combat-log.end", null);
        }, combatLogDuration * 20L));
    }

    public void removeFromCombatLog(final Player player) {
        Optional.ofNullable(combatLog.remove(player.getUniqueId())).ifPresent(BukkitTask::cancel);
    }

    public boolean isInCombatLog(final Player player) {
        return combatLog.containsKey(player.getUniqueId());
    }

}
