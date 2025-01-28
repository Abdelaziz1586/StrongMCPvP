package pebbleprojects.strongMCPvP.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.databaseData.Scramble;
import pebbleprojects.strongMCPvP.handlers.MessageHandler;
import pebbleprojects.strongMCPvP.handlers.PermissionsHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class ScrambleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        TaskHandler.INSTANCE.runAsync(() -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("/reload is the only command possible on console");
                return;
            }

            final Player player = (Player) sender;
            if (!PermissionsHandler.INSTANCE.hasPermission(player, "scramble")) {
                MessageHandler.INSTANCE.sendMessage(player, "scramble.failed.no-permission", null);
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "scramble.success." + (Scramble.INSTANCE.toggle(player.getUniqueId()) ? "enable" : "disable"), null);
        });
        return false;
    }
}
