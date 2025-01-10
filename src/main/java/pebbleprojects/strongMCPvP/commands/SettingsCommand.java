package pebbleprojects.strongMCPvP.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.handlers.SettingsHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class SettingsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        TaskHandler.INSTANCE.runAsync(() -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("/reload is the only command possible on console");
                return;
            }

            SettingsHandler.INSTANCE.openGUI((Player) sender);
        });
        return false;
    }
}
