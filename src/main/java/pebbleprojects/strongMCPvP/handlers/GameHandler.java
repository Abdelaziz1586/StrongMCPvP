package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GameHandler {

    private int fixDelay;
    private Location spawn;
    public static GameHandler INSTANCE;
    private final Map<UUID, BukkitTask> fixDelays;

    public GameHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Game Handler...");

        INSTANCE = this;

        fixDelays = new ConcurrentHashMap<>();

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Game Handler");
    }

    public void update() {
        fixDelay = DataHandler.INSTANCE.getConfig().getInt("fix-delay", 10) * 20;
        spawn = LocationHandler.INSTANCE.convertToLocation(DataHandler.INSTANCE.getData().getString("spawn"));
    }

    public void setSpawn(final Location spawn) {
        this.spawn = spawn;

        DataHandler.INSTANCE.getData().set("spawn", LocationHandler.INSTANCE.convertToString(spawn));
        DataHandler.INSTANCE.saveData();
    }

    public Location getSpawn() {
        return spawn;
    }

    public boolean addToFixDelay(final UUID uuid) {
        if (fixDelays.containsKey(uuid)) return false;

        fixDelays.put(uuid, TaskHandler.INSTANCE.runLaterAsync(() -> fixDelays.remove(uuid), fixDelay));
        return true;
    }

    public void join(final Player player) {
        PacketHandler.INSTANCE.inject(player);
        KitsHandler.INSTANCE.applyKit(player);
        DatabaseHandler.INSTANCE.load(player);
        PerksHandler.INSTANCE.setPerk(player);
        ScoreboardHandler.INSTANCE.setScoreboard(player);
        TaskHandler.INSTANCE.runLaterAsync(() -> PerksHandler.INSTANCE.onPlayerSpawn(player), 1);
        TaskHandler.INSTANCE.runSync(() -> player.teleport(spawn));
    }

    public void leave(final Player player) {
        KillsHandler.INSTANCE.addHit(player, null, true);
        DatabaseHandler.INSTANCE.save(player);
        NPCHandler.INSTANCE.unloadNPCs(player);
        PacketHandler.INSTANCE.uninject(player);
        ScoreboardHandler.INSTANCE.removeScoreboard(player);
        ChatInputHandler.INSTANCE.removeInputRequest(player);

        if (fixDelays.containsKey(player.getUniqueId())) {
            fixDelays.get(player.getUniqueId()).cancel();
            fixDelays.remove(player.getUniqueId());
        }
    }

    public void broadcast(final String messageKey, final String[] args) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            MessageHandler.INSTANCE.sendMessage(player, messageKey, args);
        }
    }

}
