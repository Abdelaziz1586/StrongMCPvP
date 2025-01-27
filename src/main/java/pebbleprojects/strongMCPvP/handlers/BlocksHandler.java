package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public final class BlocksHandler {

    public static BlocksHandler INSTANCE;
    private final Set<Location> cachedBlocksLocations;

    public BlocksHandler() {
        INSTANCE = this;

        cachedBlocksLocations = new HashSet<>();
    }

    public boolean isCached(final Location location) {
        return cachedBlocksLocations.contains(location);
    }

    public void addToCache(final Location location) {
        cachedBlocksLocations.add(location);
    }

    public void removeFromCache(final Location location) {
        cachedBlocksLocations.remove(location);
    }

}
