package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        event.getDrops().clear();
    }

}
