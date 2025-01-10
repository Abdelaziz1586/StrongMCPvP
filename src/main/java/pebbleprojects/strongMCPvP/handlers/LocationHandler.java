package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class LocationHandler {

    public static LocationHandler INSTANCE;

    public LocationHandler() {
        INSTANCE = this;
    }

    public String convertToString(final Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    public Location convertToLocation(final String format) {
        if (!format.contains(",")) return null;

        final String[] split = format.split(",");

        if (split.length != 6) return null;

        final World world = Bukkit.getWorld(split[0]);

        if (world == null) return null;

        try {
            return new Location(world, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
        } catch (final NumberFormatException ignored) {
            return null;
        }
    }

}
