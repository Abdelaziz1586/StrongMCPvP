package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatLogHandler {

    private int combatLogDuration;
    public static CombatLogHandler INSTANCE;
    private final Map<UUID, BukkitTask> combatLog;

    public CombatLogHandler() {
        INSTANCE = this;

        combatLog = new ConcurrentHashMap<>();

        update();
    }

    public void update() {
        combatLogDuration = DataHandler.INSTANCE.getConfig().getInt("gameplay.combat-log", 10);
    }

    public void addToCombatLog(final Player player) {
        final UUID uuid = player.getUniqueId();

        if (!combatLog.containsKey(uuid)) {
            MessageHandler.INSTANCE.sendMessage(player, "gameplay.combat-log.start", new String[]{"combatLog," + combatLogDuration});
        } else {
            Optional.ofNullable(combatLog.remove(uuid)).ifPresent(BukkitTask::cancel);
        }

        player.setLevel(combatLogDuration);
        player.setExp(1.0f);

        combatLog.put(uuid, TaskHandler.INSTANCE.runTaskTimerAsync(new BukkitRunnable() {
            private int remainingTicks = combatLogDuration * 20;

            @Override
            public void run() {
                if (remainingTicks <= 0) {
                    combatLog.remove(uuid);

                    player.setLevel(0);
                    player.setExp(0.0f);

                    MessageHandler.INSTANCE.sendMessage(player, "gameplay.combat-log.end", null);
                    cancel();
                    return;
                }

                player.setExp((float) remainingTicks / (combatLogDuration * 20));
                player.setLevel((remainingTicks + 19) / 20);

                remainingTicks--;
            }
        }, 1L));
    }

    public void removeFromCombatLog(final Player player) {
        Optional.ofNullable(combatLog.remove(player.getUniqueId())).ifPresent(task -> {
            task.cancel();
            player.setLevel(0);
            player.setExp(0.0f);
        });
    }

    public boolean isInCombatLog(final Player player) {
        return combatLog.containsKey(player.getUniqueId());
    }
}
