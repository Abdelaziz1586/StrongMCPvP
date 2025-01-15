package pebbleprojects.strongMCPvP.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.handlers.CombatLogHandler;
import pebbleprojects.strongMCPvP.handlers.GameHandler;
import pebbleprojects.strongMCPvP.handlers.MessageHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class FixCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        TaskHandler.INSTANCE.runSync(() -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("/reload is the only command possible by console");
                return;
            }

            final Player player = (Player) sender;

            if (CombatLogHandler.INSTANCE.isInCombatLog(player)) {
                MessageHandler.INSTANCE.sendMessage(player, "fix.failed.combat-log", null);
                return;
            }

            if (GameHandler.INSTANCE.addToFixDelay(player.getUniqueId())) {
                for (final Player online : Bukkit.getOnlinePlayers()) {
                    online.hidePlayer(player);
                }

                TaskHandler.INSTANCE.runLaterSync(() -> {
                    for (final Player online : Bukkit.getOnlinePlayers()) {
                        online.showPlayer(player);
                    }

                    MessageHandler.INSTANCE.sendMessage(player, "fix.success", null);
                }, 1);
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "fix.failed.delay", null);
        });
        return false;
    }
}
