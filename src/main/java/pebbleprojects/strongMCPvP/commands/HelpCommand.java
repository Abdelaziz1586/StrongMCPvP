package pebbleprojects.strongMCPvP.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pebbleprojects.strongMCPvP.handlers.MessageHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

public final class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        TaskHandler.INSTANCE.runAsync(() -> MessageHandler.INSTANCE.sendHelpMessage(sender, args));
        return false;
    }
}
