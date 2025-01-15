package pebbleprojects.strongMCPvP.functions.profile;

import java.util.UUID;

public final class RealProfile {

    private final UUID uuid;
    private final String name;

    public RealProfile(final UUID uuid, final String name) {
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
