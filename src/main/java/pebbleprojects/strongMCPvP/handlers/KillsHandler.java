package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.databaseData.Deaths;
import pebbleprojects.strongMCPvP.databaseData.Kills;
import pebbleprojects.strongMCPvP.databaseData.Points;
import pebbleprojects.strongMCPvP.databaseData.Souls;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.hit.HitData;
import pebbleprojects.strongMCPvP.functions.hit.Hits;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class KillsHandler {

    private final Map<UUID, Hits> hits;
    public static KillsHandler INSTANCE;
    private boolean divideSoulsFairly, dividePointsFairly;
    private int killSouls, killPoints, assistSouls, assistPoints;

    public KillsHandler() {
        INSTANCE = this;

        hits = new ConcurrentHashMap<>();

        update();
    }

    public void update() {
        updateSouls();
        updatePoints();
    }

    public void addHit(final Player victim, final Player attacker, final boolean deathHit) {
        final Hits hits = this.hits.getOrDefault(victim.getUniqueId(), new Hits());

        if (attacker != null) {
            CombatLogHandler.INSTANCE.addToCombatLog(victim);
            CombatLogHandler.INSTANCE.addToCombatLog(attacker);

            hits.addHit(attacker.getUniqueId());

            this.hits.put(victim.getUniqueId(), hits);
        }

        if (deathHit) addDeath(victim, attacker, hits);
    }

    private void addDeath(final Player victim, Player attacker, final Hits hits) {
        victim.setHealth(20);
        victim.teleport(GameHandler.INSTANCE.getSpawn());

        KitsHandler.INSTANCE.applyKit(victim);
        Deaths.INSTANCE.add(victim.getUniqueId(), 1);
        KillStreakHandler.INSTANCE.removeKillStreaks(victim);
        CombatLogHandler.INSTANCE.removeFromCombatLog(victim);

        if (attacker == null && !hits.getHits().isEmpty()) {
            attacker = Bukkit.getPlayer(hits.getHits().get(hits.getHits().size() - 1).getUUID());
        }

        if (attacker != null) {
            KitsHandler.INSTANCE.kill(attacker);
            Kills.INSTANCE.add(attacker.getUniqueId(), 1);
            KillStreakHandler.INSTANCE.addKillStreak(victim, attacker);

            final String[] args = {
                    "attacker," + attacker.getDisplayName(),
                    "victim," + victim.getDisplayName()
            };

            GameHandler.INSTANCE.broadcast("gameplay.kill.broadcast", args);
            MessageHandler.INSTANCE.sendMessage(victim, "gameplay.death.attacked", args);
        }

        divideFairly(hits, victim, attacker);
    }

    private void divideFairly(final Hits hits, final Player victim, final Player attacker) {
        if (hits.getHits().size() == 1 || (!divideSoulsFairly && !dividePointsFairly)) {
            Souls.INSTANCE.add(attacker.getUniqueId(), killSouls);
            Points.INSTANCE.add(attacker.getUniqueId(), killPoints);

            MessageHandler.INSTANCE.sendMessage(attacker, "gameplay.kill.self", new String[]{"attacker," + attacker.getDisplayName(), "victim," + victim.getDisplayName(), "souls," + killSouls, "points," + killPoints});
        }

        if (hits.getHits().size() == 1) return;

        int totalHits = 0;
        final Map<UUID, Integer> hitCounts = new HashMap<>();

        while (!hits.getHits().isEmpty()) {
            final HitData hitData = hits.getHits().pop();
            final int hitCount = hitData.getHits();

            totalHits += hitCount;
            hitCounts.merge(hitData.getUUID(), hitCount, Integer::sum);
        }

        if (totalHits == 0) return;

        for (final Map.Entry<UUID, Integer> entry : hitCounts.entrySet()) {
            final int share = entry.getValue() / totalHits,
                    souls = divideSoulsFairly ? killSouls * share : assistSouls,
                    points = dividePointsFairly ? killPoints * share : assistPoints;

            if (souls == 0 || points == 0) continue;

            Souls.INSTANCE.add(entry.getKey(), souls);
            Points.INSTANCE.add(entry.getKey(), points);

            MessageHandler.INSTANCE.sendMessage(Bukkit.getPlayer(entry.getKey()), "gameplay.assist", new String[]{"victim," + victim.getDisplayName(), "souls," + souls, "points," + points});
        }
    }

    private void updateSouls() {
        final Configuration section = DataHandler.INSTANCE.getConfig().getSection("gameplay.souls");
        if (section == null) {
            killSouls = 1;
            assistSouls = 0;
            divideSoulsFairly = false;
            return;
        }

        killSouls = Math.max(section.getInt("kill", 0), 0);
        assistSouls = Math.max(section.getInt("assist", 0), 0);
        divideSoulsFairly = section.getBoolean("divide-fairly", false);
    }

    private void updatePoints() {
        final Configuration section = DataHandler.INSTANCE.getConfig().getSection("gameplay.points");
        if (section == null) {
            killPoints = 20;
            assistPoints = 10;
            dividePointsFairly = true;
            return;
        }

        killPoints = Math.max(section.getInt("kill", 0), 0);
        assistPoints = Math.max(section.getInt("assist", 0), 0);
        dividePointsFairly = section.getBoolean("divide-fairly", true);
    }
}
