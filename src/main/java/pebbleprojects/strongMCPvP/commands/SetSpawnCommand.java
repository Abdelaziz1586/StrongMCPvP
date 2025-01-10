package pebbleprojects.strongMCPvP.commands;

import pebbleprojects.strongMCPvP.handlers.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SetSpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        TaskHandler.INSTANCE.runAsync(() -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("/reload is the only command possible on console");
                return;
            }

            final Player player = (Player) sender;

            if (!PermissionsHandler.INSTANCE.hasPermission(player, "set-spawn")) {
                MessageHandler.INSTANCE.sendMessage(player, "set-spawn.no-permission", null);
                return;
            }

            GameHandler.INSTANCE.setSpawn(player.getLocation());
            MessageHandler.INSTANCE.sendMessage(player, "set-spawn.success", null);
        });
        return false;
    }
}
