package pebbleprojects.strongMCPvP.functions;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.MessageHandler;
import pebbleprojects.strongMCPvP.handlers.UtilsHandler;
import pebbleprojects.strongMCPvP.handlers.papi.PlaceholderAPIHandler;

public final class ConfigMessage {

    private final int stay, fadeIn, fadeOut;
    private final String chat, actionbar, title, subtitle;

    public ConfigMessage(final Configuration section) {
        chat = ChatColor.translateAlternateColorCodes('&', section.getString("chat", "").replace("%prefix%", MessageHandler.INSTANCE.getPrefix()));
        actionbar = ChatColor.translateAlternateColorCodes('&', section.getString("actionbar", "").replace("%prefix%", MessageHandler.INSTANCE.getPrefix()));

        final Configuration titleScreen = section.getSection("title-screen");

        if (titleScreen == null) {
            title = "";
            subtitle = "";

            stay = 3;
            fadeIn = 1;
            fadeOut = 1;

            return;
        }

        title = ChatColor.translateAlternateColorCodes('&', titleScreen.getString("title", "").replace("%prefix%", MessageHandler.INSTANCE.getPrefix()));
        subtitle = ChatColor.translateAlternateColorCodes('&', titleScreen.getString("subtitle", "").replace("%prefix%", MessageHandler.INSTANCE.getPrefix()));

        stay = titleScreen.getInt("stay", 3);
        fadeIn = titleScreen.getInt("fadeIn", 1);
        fadeOut = titleScreen.getInt("fadeOut", 1);
    }

    public void send(final CommandSender sender, final String[] args) {
        if (!chat.isEmpty()) sender.sendMessage(replaceWithArgs(sender, chat, args));

        if (sender instanceof Player) {
            final Player player = (Player) sender;

            UtilsHandler.INSTANCE.sendActionBar(player, replaceWithArgs(sender, actionbar, args));
            UtilsHandler.INSTANCE.sendTitle(player, replaceWithArgs(sender, title, args), replaceWithArgs(sender, subtitle, args), fadeIn, stay, fadeOut);
        }
    }

    private String replaceWithArgs(final CommandSender sender, String text, final String... args) {
        String[] split;
        for (final String s : args) {
            if (s.contains(",")) {
                split = s.split(",");

                text = text.replace("%" + split[0] + "%", split[1]);
            }
        }

        return PlaceholderAPIHandler.INSTANCE.translateMessage(sender, text);
    }

}
