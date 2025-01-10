package pebbleprojects.strongMCPvP.functions.hit;

import java.util.Stack;
import java.util.UUID;

public final class Hits {

    private final Stack<HitData> hits;

    public Hits() {
        hits = new Stack<>();
    }

    public void addHit(final UUID uuid) {
        for (final HitData hitData : hits) {
            if (uuid.equals(hitData.getUUID())) {
                hitData.increment();

                hits.remove(hitData);
                hits.push(hitData);
                return;
            }
        }

        final HitData hitData = new HitData(uuid);
        hitData.increment();

        hits.push(hitData);
    }

    public Stack<HitData> getHits() {
        return hits;
    }
}
