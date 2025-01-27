package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.plugin.PluginManager;
import pebbleprojects.strongMCPvP.PvP;
import pebbleprojects.strongMCPvP.listeners.*;

public final class ListenersHandler {

    public ListenersHandler() {
        final PvP main = DataHandler.INSTANCE.getMain();
        final PluginManager pm = main.getServer().getPluginManager();

        pm.registerEvents(new PlayerJoin(), main);
        pm.registerEvents(new PlayerQuit(), main);
        pm.registerEvents(new BlockPlace(), main);
        pm.registerEvents(new BlockFromTo(), main);
        pm.registerEvents(new PlayerDeath(), main);
        pm.registerEvents(new EntityDamage(), main);
        pm.registerEvents(new RightClickNPC(), main);
        pm.registerEvents(new ProjectileHit(), main);
        pm.registerEvents(new ServerCommand(), main);
        pm.registerEvents(new PlayerInteract(), main);
        pm.registerEvents(new InventoryClick(), main);
        pm.registerEvents(new AsyncPlayerChat(), main);
        pm.registerEvents(new FoodLevelChange(), main);
        pm.registerEvents(new ProjectileLaunch(), main);
        pm.registerEvents(new PlayerSpawnLocation(), main);
        pm.registerEvents(new PlayerCommandPreprocess(), main);
    }

}
