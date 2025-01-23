package pebbleprojects.strongMCPvP.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.handlers.CombatLogHandler;
import pebbleprojects.strongMCPvP.handlers.GameHandler;
import pebbleprojects.strongMCPvP.handlers.MessageHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        TaskHandler.INSTANCE.runAsync(() -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("/reload is the only command possible on console");
                return;
            }

            final Player player = (Player) sender;

            if (CombatLogHandler.INSTANCE.isInCombatLog(player)) {
                MessageHandler.INSTANCE.sendMessage(player, "spawn.failed.combat-log", null);
                return;
            }

            if (GameHandler.INSTANCE.addDelay(player.getUniqueId(), 1)) {
                GameHandler.INSTANCE.sendToSpawn(player);
                MessageHandler.INSTANCE.sendMessage(player, "spawn.success", null);
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "spawn.failed.delay", null);
        });
        return false;
    }
}
