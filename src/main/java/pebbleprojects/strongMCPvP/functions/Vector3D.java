package pebbleprojects.strongMCPvP.functions;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Objects;

public final class Vector3D {

    public final double x, y, z;

    public Vector3D(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(final Location location) {
        this(Objects.requireNonNull(location.toVector(), "Location vector cannot be NULL."));
    }

    public Vector3D(final Vector vector) {
        if (vector == null) throw new IllegalArgumentException("Vector cannot be NULL.");
        this.x = vector.getX();
        this.y = vector.getY();
        this.z = vector.getZ();
    }

    public Vector3D add(final Vector3D other) {
        Objects.requireNonNull(other, "Other cannot be NULL");
        if (other.x == 0 && other.y == 0 && other.z == 0) return this;
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }

    public Vector3D add(final double x, final double y, final double z) {
        if (x == 0 && y == 0 && z == 0) return this;
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D subtract(final Vector3D other) {
        Objects.requireNonNull(other, "Other cannot be NULL");
        if (other.x == 0 && other.y == 0 && other.z == 0) return this;
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }

    public Vector3D multiply(final int factor) {
        if (factor == 1) return this;
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    public Vector3D multiply(final double factor) {
        if (factor == 1.0) return this;
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    public Vector3D abs() {
        if (x >= 0 && y >= 0 && z >= 0) return this;
        return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
    }
}
