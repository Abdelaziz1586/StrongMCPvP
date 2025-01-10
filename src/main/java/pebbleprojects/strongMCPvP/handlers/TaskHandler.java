package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public final class TaskHandler {

    public static TaskHandler INSTANCE;
    private final BukkitScheduler scheduler;

    public TaskHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Task Handler...");

        INSTANCE = this;

        scheduler = DataHandler.INSTANCE.getMain().getServer().getScheduler();

        DataHandler.INSTANCE.getLogger().info("Loaded Task Handler!");
    }

    public void runSync(final Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            scheduler.runTask(DataHandler.INSTANCE.getMain(), runnable);
            return;
        }

        runnable.run();
    }

    public void runAsync(final Runnable runnable) {
        scheduler.runTaskAsynchronously(DataHandler.INSTANCE.getMain(), runnable);
    }

    public BukkitTask runLaterAsync(final Runnable runnable, final long delay) {
        return scheduler.runTaskLaterAsynchronously(DataHandler.INSTANCE.getMain(), runnable, delay);
    }

    public BukkitTask runLaterSync(final Runnable runnable, final long delay) {
        return scheduler.runTaskLater(DataHandler.INSTANCE.getMain(), runnable, delay);
    }

    public void runTaskTimerAsync(final BukkitRunnable runnable, final long delay) {
        runnable.runTaskTimer(DataHandler.INSTANCE.getMain(), 0, delay);
    }
}
