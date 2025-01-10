package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.databaseData.Kills;
import pebbleprojects.strongMCPvP.databaseData.Points;
import pebbleprojects.strongMCPvP.databaseData.Souls;
import pebbleprojects.strongMCPvP.functions.config.Configuration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KillStreakHandler {

    public static KillStreakHandler INSTANCE;
    private final Map<UUID, Integer> bounties, killStreaks;
    private int every, extraSouls, extraPoints, soulBountyIncrease;

    public KillStreakHandler() {
        INSTANCE = this;

        bounties = new ConcurrentHashMap<>();
        killStreaks = new ConcurrentHashMap<>();

        update();
    }

    public void update() {
        final Configuration section = DataHandler.INSTANCE.getConfig().getSection("gameplay.kill-streaks");
        if (section == null) {
            every = 5;
            extraSouls = 0;
            extraPoints = 0;
            soulBountyIncrease = 10;
            return;
        }

        every = Math.max(section.getInt("every", 5), 0);
        extraSouls = Math.max(section.getInt("extra-souls", 0), 0);
        extraPoints = Math.max(section.getInt("extra-points", 0), 0);
        soulBountyIncrease = Math.max(section.getInt("soul-bounty-increase", 10), 0);
    }

    public void addKillStreak(final Player victim, final Player attacker) {
        final UUID uuid = attacker.getUniqueId();
        final int killStreaks = this.killStreaks.getOrDefault(uuid, 0) + 1;

        this.killStreaks.put(uuid, killStreaks);

        if (every != 0 && killStreaks % every == 0) {
            addBounty(uuid);
            giveExtraRewards(uuid);

            sendKillStreakMessage(victim, attacker);
        }
    }

    public void removeKillStreaks(final Player player) {
        killStreaks.remove(player.getUniqueId());
    }

    public int getKillStreaks(final UUID uuid) {
        return killStreaks.getOrDefault(uuid, 0);
    }

    private void giveExtraRewards(final UUID uuid) {
        if (extraSouls > 0) Souls.INSTANCE.add(uuid, extraSouls);
        if (extraPoints > 0) Points.INSTANCE.add(uuid, extraPoints);
    }

    private void addBounty(final UUID uuid) {
        if (soulBountyIncrease != 0) bounties.merge(uuid, soulBountyIncrease, Integer::sum);
    }

    private void sendKillStreakMessage(final Player victim, final Player attacker) {
        final UUID attackerId = attacker.getUniqueId();
        final int bounty = bounties.getOrDefault(attackerId, 0);
        final String[] args = {
                "victim," + victim.getDisplayName(),
                "attacker," + attacker.getDisplayName(),
                "killStreaks," + killStreaks.get(attackerId),
                "bounty," + bounty
        };

        GameHandler.INSTANCE.broadcast("gameplay.kill-streak.broadcast", args);
        MessageHandler.INSTANCE.sendMessage(attacker, "gameplay.kill-streak.self", args);

        if (bounty != 0) {
            GameHandler.INSTANCE.broadcast("gameplay.bounty.increase.broadcast", args);
            MessageHandler.INSTANCE.sendMessage(attacker, "gameplay.bounty.increase.self", args);
        }
    }
}
