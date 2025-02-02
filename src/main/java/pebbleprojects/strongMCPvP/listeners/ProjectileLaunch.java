package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;
import pebbleprojects.strongMCPvP.handlers.TrailsHandler;

public final class ProjectileLaunch implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        TaskHandler.INSTANCE.runAsync(() -> {
            final Projectile projectile = event.getEntity();

            if (projectile.getType() == EntityType.FISHING_HOOK) return;

            final ProjectileSource source = projectile.getShooter();

            if (source instanceof Player && TrailsHandler.INSTANCE.playParticle(((Player) source).getUniqueId(), projectile) && projectile.getType() == EntityType.ARROW)
                ((Arrow) projectile).setCritical(false);
        });
    }

}
