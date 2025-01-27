package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pebbleprojects.strongMCPvP.databaseData.Scramble;
import pebbleprojects.strongMCPvP.handlers.ChatInputHandler;
import pebbleprojects.strongMCPvP.handlers.LevelsHandler;
import pebbleprojects.strongMCPvP.handlers.MessageHandler;
import pebbleprojects.strongMCPvP.handlers.papi.PlaceholderAPIHandler;

public final class AsyncPlayerChat implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        if (ChatInputHandler.INSTANCE.onAsyncPlayerChat(event)) return;

        final String message = buildChatMessage(event.getPlayer(), event.getMessage());

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    private String buildChatMessage(final Player sender, final String originalMessage) {
        final String messageFormat;
        if (Scramble.INSTANCE.get(sender.getUniqueId())) {
            messageFormat = ChatColor.translateAlternateColorCodes('&',
                    MessageHandler.INSTANCE.getMessages()
                            .getString("chat-scramble-message", "&e# %player%&7: &f%message%")
            );
        } else {
            messageFormat = LevelsHandler.INSTANCE.getChatPrefix(sender.getUniqueId());
        }

        return PlaceholderAPIHandler.INSTANCE.translateMessage(sender,
                messageFormat
                        .replace("%player%", sender.getDisplayName())
                        .replace("%message%", originalMessage)
        );
    }
}
