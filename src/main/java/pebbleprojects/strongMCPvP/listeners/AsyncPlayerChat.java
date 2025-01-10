package pebbleprojects.strongMCPvP.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pebbleprojects.strongMCPvP.handlers.ChatInputHandler;

public final class AsyncPlayerChat implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        if (ChatInputHandler.INSTANCE.onAsyncPlayerChat(event)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        final Player sender = event.getPlayer();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(sender.getDisplayName() + ": " + mentionUsers(ChatColor.translateAlternateColorCodes('&', event.getMessage())));
        }
    }

    public String mentionUsers(String message) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String playerName = player.getName(),
                    regex = "@" + playerName;

            int mentionIndex = message.toLowerCase().indexOf("@" + playerName.toLowerCase());
            while (mentionIndex != -1) {
                String colorBeforeMention = ChatColor.getLastColors(message.substring(0, mentionIndex));

                if (colorBeforeMention.isEmpty()) {
                    colorBeforeMention = ChatColor.RESET.toString();
                }

                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);

                final String replacement = ChatColor.YELLOW + playerName + colorBeforeMention;
                message = message.substring(0, mentionIndex) + replacement + message.substring(mentionIndex + regex.length());

                mentionIndex = message.toLowerCase().indexOf("@" + playerName.toLowerCase(), mentionIndex + replacement.length());
            }
        }
        return message;
    }
}
