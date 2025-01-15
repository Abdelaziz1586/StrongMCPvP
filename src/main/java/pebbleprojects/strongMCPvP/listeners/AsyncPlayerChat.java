package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pebbleprojects.strongMCPvP.handlers.ChatInputHandler;
import pebbleprojects.strongMCPvP.handlers.LevelsHandler;

public final class AsyncPlayerChat implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        if (ChatInputHandler.INSTANCE.onAsyncPlayerChat(event)) return;

        final Player sender = event.getPlayer();
        final String message = LevelsHandler.INSTANCE.getChatPrefix(sender.getUniqueId())
                .replace("%player%", sender.getDisplayName())
                .replace("%message%", event.getMessage());

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
}
