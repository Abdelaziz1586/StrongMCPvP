package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pebbleprojects.strongMCPvP.customEvents.RightClickNPCEvent;
import pebbleprojects.strongMCPvP.handlers.*;

public final class RightClickNPC implements Listener {

    @EventHandler
    public void onRightClickNPC(final RightClickNPCEvent event) {
        TaskHandler.INSTANCE.runAsync(() -> {
            switch (event.getNPC().getType()) {
                case 0:
                    ShopHandler.INSTANCE.openGUI(event.getPlayer(), false);
                    break;
                case 1:
                    PerksHandler.INSTANCE.openGUI(event.getPlayer(), 0);
                    break;
                case 2:
                    TrailsHandler.INSTANCE.openGUI(event.getPlayer());
                    break;
                case 3:
                    SettingsHandler.INSTANCE.openGUI(event.getPlayer());
                    break;
            }
        });
    }

}
