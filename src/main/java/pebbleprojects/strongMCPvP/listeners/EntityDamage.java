package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import pebbleprojects.strongMCPvP.handlers.GameHandler;
import pebbleprojects.strongMCPvP.handlers.KillsHandler;
import pebbleprojects.strongMCPvP.handlers.PerksHandler;

public final class EntityDamage implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            PerksHandler.INSTANCE.onEntityDamage(event);

            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                return;
            }

            final Player victim = (Player) entity;
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                entity = ((EntityDamageByEntityEvent) event).getDamager();

                if (entity instanceof Projectile) {
                    final ProjectileSource source = ((Projectile) entity).getShooter();
                    entity = source instanceof Player ? (Player) source : entity;
                }

                if (!(entity instanceof Player)) {
                    event.setCancelled(true);
                    return;
                }

                final Player attacker = (Player) entity;

                if (GameHandler.INSTANCE.isSpectator(attacker)) {
                    event.setCancelled(true);
                    return;
                }

                if (event.getFinalDamage() > victim.getHealth()) {
                    event.setCancelled(true);

                    if (attacker != victim)
                        KillsHandler.INSTANCE.addHit(victim, attacker, true);

                    PerksHandler.INSTANCE.onPlayerDeath(victim, attacker);
                    return;
                }

                if (attacker != victim)
                    KillsHandler.INSTANCE.addHit(victim, attacker, false);

                return;
            }

            final boolean b = event.getFinalDamage() > victim.getHealth();

            event.setCancelled(b);
            KillsHandler.INSTANCE.addHit(victim, null, b);
        }
    }

}
