package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GameHandler {

    private Location spawn;
    private final int[] delays;
    public static GameHandler INSTANCE;
    private final Map<UUID, Map<Integer, BukkitTask>> delaysMap;

    public GameHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Game Handler...");

        INSTANCE = this;

        delays = new int[3];
        delaysMap = new ConcurrentHashMap<>();

        update();

        for (final Player player : Bukkit.getOnlinePlayers())
            join(player);

        DataHandler.INSTANCE.getLogger().info("Loaded Game Handler");
    }

    public void update() {
        delays[0] = DataHandler.INSTANCE.getConfig().getInt("gameplay.fix-delay", 10) * 20;
        delays[1] = DataHandler.INSTANCE.getConfig().getInt("gameplay.spawn-delay", 10) * 20;
        delays[2] = DataHandler.INSTANCE.getConfig().getInt("gameplay.spectate-delay", 10) * 20;

        spawn = LocationHandler.INSTANCE.convertToLocation(DataHandler.INSTANCE.getData().getString("spawn"));
    }

    public void setSpawn(final Location spawn) {
        this.spawn = spawn;

        DataHandler.INSTANCE.getData().set("spawn", LocationHandler.INSTANCE.convertToString(spawn));
        DataHandler.INSTANCE.saveData();
    }

    public void sendToSpawn(final Player player) {
        if (spawn != null) {
            TaskHandler.INSTANCE.runSync(() -> {
                player.teleport(spawn);
                player.setGameMode(GameMode.SURVIVAL);
            });
        }
    }

    public boolean addDelay(final UUID uuid, final int delayId) {
        if (delays[delayId] <= 0) return true;

        final Map<Integer, BukkitTask> delays = delaysMap.getOrDefault(uuid, new ConcurrentHashMap<>());

        if (delays.containsKey(delayId)) return false;

        delays.put(delayId, TaskHandler.INSTANCE.runLaterAsync(() -> removeDelay(uuid, delayId), this.delays[delayId]));
        delaysMap.put(uuid, delays);
        return true;
    }

    private void removeDelay(final UUID uuid, final int delayId) {
        final Map<Integer, BukkitTask> delays = delaysMap.getOrDefault(uuid, new ConcurrentHashMap<>());

        if (delays.containsKey(delayId)) {
            delays.remove(delayId).cancel();

            delaysMap.put(uuid, delays);
        }
    }

    public void join(final Player player) {
        PacketHandler.INSTANCE.inject(player);
        KitsHandler.INSTANCE.applyKit(player);
        DatabaseHandler.INSTANCE.load(player);
        PerksHandler.INSTANCE.setPerk(player);
        ScoreboardHandler.INSTANCE.setScoreboard(player);
        TaskHandler.INSTANCE.runLaterAsync(() -> PerksHandler.INSTANCE.onPlayerSpawn(player), 1);
        TaskHandler.INSTANCE.runSync(() -> {
            player.setGameMode(GameMode.SURVIVAL);

            player.teleport(spawn);
        });

        player.setLevel(0);
        player.setExp(0.0f);
    }

    public void leave(final Player player) {
        KillsHandler.INSTANCE.addHit(player, null, true);
        DatabaseHandler.INSTANCE.save(player);
        NPCHandler.INSTANCE.unloadNPCs(player);
        PacketHandler.INSTANCE.uninject(player);
        ScoreboardHandler.INSTANCE.removeScoreboard(player);
        ChatInputHandler.INSTANCE.removeInputRequest(player);


        if (delaysMap.containsKey(player.getUniqueId())) {
            for (final BukkitTask task : delaysMap.get(player.getUniqueId()).values())
                task.cancel();

            delaysMap.remove(player.getUniqueId());
        }
    }

    public void broadcast(final String messageKey, final String[] args) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            MessageHandler.INSTANCE.sendMessage(player, messageKey, args);
        }
    }

}
