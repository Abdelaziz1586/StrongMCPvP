package pebbleprojects.strongMCPvP.functions;

import java.util.UUID;

public final class TopPlayerData {

    private final UUID uuid;
    private final String name;

    public TopPlayerData(final UUID uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
