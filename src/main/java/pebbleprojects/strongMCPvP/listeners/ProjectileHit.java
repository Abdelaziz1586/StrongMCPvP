package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class ProjectileHit implements Listener {

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();

        if (projectile.getType() == EntityType.FISHING_HOOK) return;

        final ProjectileSource source = projectile.getShooter();

        if (source instanceof Player)
            projectile.remove();
    }

}
