package pebbleprojects.strongMCPvP.functions.hit;

import java.util.UUID;

public final class HitData {

    private int hits;
    private final UUID uuid;

    public HitData(final UUID uuid) {
        this.hits = 0;
        this.uuid = uuid;
    }

    public void increment() {
        hits++;
    }

    public int getHits() {
        return hits;
    }

    public UUID getUUID() {
        return uuid;
    }
}
