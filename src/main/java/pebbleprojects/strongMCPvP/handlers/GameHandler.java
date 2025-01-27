package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GameHandler {

    private Location spawn;
    private final int[] delays;
    public static GameHandler INSTANCE;
    private final Set<Player> spectators;
    private final Map<UUID, Map<Integer, BukkitTask>> delaysMap;

    public GameHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Game Handler...");

        INSTANCE = this;

        delays = new int[3];
        delaysMap = new ConcurrentHashMap<>();

        spectators = new HashSet<>();

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

    public boolean toggleSpectate(final Player player) {
        boolean b = spectators.add(player);

        TaskHandler.INSTANCE.runSync(() -> {
            for (final PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());

            if (b) {
                addGhost(player);
                clearInventory(player);
                player.setGameMode(GameMode.ADVENTURE);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            } else {
                TaskHandler.INSTANCE.runLaterAsync(() -> {
                    KitsHandler.INSTANCE.applyKit(player);
                    PerksHandler.INSTANCE.onPlayerSpawn(player);
                }, 1);

                player.setGameMode(GameMode.SURVIVAL);

                removeGhost(player);
            }

            player.setAllowFlight(b);

            player.setFlying(b);
            toggleVisibility(player, !b);

            if (spawn != null)
                player.teleport(spawn);
        });

        if (!b) spectators.remove(player);

        return b;
    }

    public boolean isSpectator(final Player player) {
        return spectators.contains(player);
    }

    private void addGhost(final Player player) {
        final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team ghostTeam = scoreboard.getTeam("PvP Ghosts");

        if (ghostTeam == null) {
            ghostTeam = scoreboard.registerNewTeam("PvP Ghosts");
            ghostTeam.setCanSeeFriendlyInvisibles(true);
        }

        ghostTeam.addEntry(player.getName());
    }

    public void removeGhost(final Player player) {
        final Team ghostTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("PvP Ghosts");

        if (ghostTeam != null && ghostTeam.hasEntry(player.getName()))
            ghostTeam.removeEntry(player.getName());
    }

    private void clearInventory(final Player player) {
        final PlayerInventory inventory = player.getInventory();

        inventory.setContents(new ItemStack[]{});
        inventory.setArmorContents(null);
    }

    private void toggleVisibility(final Player player, final boolean show) {
        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (show) {
                onlinePlayer.showPlayer(player);
                continue;
            }

            onlinePlayer.hidePlayer(player);
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
            player.setAllowFlight(false);
            player.setGameMode(GameMode.SURVIVAL);

            player.teleport(spawn);

            toggleVisibility(player, true);

            for (final PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());

            for (final Player spectator : spectators)
                player.hidePlayer(spectator);
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
