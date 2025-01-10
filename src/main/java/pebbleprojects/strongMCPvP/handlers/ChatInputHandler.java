package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ChatInputHandler {

    public static ChatInputHandler INSTANCE;
    private final Map<UUID, BukkitTask> tasks;
    private final Map<UUID, Runnable> timeouts;
    private final Map<UUID, Consumer<AsyncPlayerChatEvent>> consumers;

    public ChatInputHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Chat Input Handler...");

        INSTANCE = this;

        tasks = new ConcurrentHashMap<>();
        timeouts = new ConcurrentHashMap<>();
        consumers = new ConcurrentHashMap<>();

        DataHandler.INSTANCE.getLogger().info("Loaded Chat Input Handler!");
    }

    public void inputFromThen(final Player player, final Consumer<AsyncPlayerChatEvent> consumer, final Runnable timeout) {
        final UUID uuid = player.getUniqueId();

        consumers.put(uuid, consumer);
        timeouts.put(uuid, timeout);

        player.closeInventory();

        tasks.put(uuid, TaskHandler.INSTANCE.runLaterAsync(() -> {
            if (timeouts.containsKey(uuid)) {
                timeouts.get(uuid).run();
                timeouts.remove(uuid);
                consumers.remove(uuid);

                tasks.remove(uuid);
            }
        }, 200));
    }

    public void removeInputRequest(final Player player) {
        final UUID uuid = player.getUniqueId();

        consumers.remove(uuid);
        timeouts.remove(uuid);
    }

    public boolean onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();

        if (consumers.containsKey(uuid)) {
            consumers.get(uuid).accept(event);
            timeouts.remove(uuid);
            consumers.remove(uuid);

            tasks.get(uuid).cancel();
            tasks.remove(uuid);
            return true;
        }

        return false;
    }

}
