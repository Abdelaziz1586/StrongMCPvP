package pebbleprojects.strongMCPvP.handlers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.functions.ConfigMessage;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public final class MessageHandler {

    private String prefix;
    private Configuration messages;
    private final File messagesFile;
    public static MessageHandler INSTANCE;
    private final HashMap<String, ConfigMessage> cache;
    private final BaseComponent[][] helpMessage;

    public MessageHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Message Handler...");

        INSTANCE = this;

        cache = new HashMap<>();

        messagesFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "messages.yml");

        update();

        helpMessage = new BaseComponent[2][];

        helpMessage[0] = new ComponentBuilder("\nVampiresPvP Page 1").color(ChatColor.YELLOW).bold(true)
                .append("\n")
                .append("Click to select a help option...").color(ChatColor.YELLOW).bold(false)
                .append("\n\n")
                .append("§7  §c* §bStats").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats"))
                .append("\n")
                .append("§7  §c* §bSave Inventory").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/save"))
                .append("\n")
                .append("§7  §c* §bSpectate").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spectate"))
                .append("\n")
                .append("§7  §c* §bTop Kills").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/topKills"))
                .append("\n")
                .append("§7  §c* §bTop Points").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/save"))
                .append("\n")
                .append("§7  §c* §bShow Ranks").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ranks"))
                .append("\n")
                .append("§7  §c* §bScramble Points").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scramble"))
                .append("\n")
                .append("§7  §c* §bToggle Profile Visibility").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/toggleProfile"))
                .append("\n")
                .append("§7  §c* §bGet Others' Profile").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile"))
                .append("\n").create();

        helpMessage[1] = new ComponentBuilder("\nVampiresPvP Page 2 (ADMIN COMMANDS)").color(ChatColor.YELLOW).bold(true)
                .append("\n")
                .append("Click to select a help option...").color(ChatColor.YELLOW).bold(false)
                .append("\n\n")
                .append("§7  §c* §bAdd Points").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/addPoints"))
                .append("\n")
                .append("§7  §c* §bAdd Souls").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/addSouls"))
                .append("\n")
                .append("§7  §c* §bBuild Mode").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/build"))
                .append("\n")
                .append("§7  §c* §bNPC Settings").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/npc"))
                .append("\n")
                .append("§7  §c* §bSet Spawn Location").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/setSpawn"))
                .append("\n")
                .append("§7  §c* §bReload Configs").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dClick to select!"))).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reload"))
                .append("\n")
                .create();

        DataHandler.INSTANCE.getLogger().info("Loaded Message Handler!");
    }

    public void update() {
        cache.clear();

        if (!messagesFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("messages.yml", messagesFile);
        }

        try {
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(messagesFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load messages.yml in memory: " + e.getMessage());
            return;
        }

        prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", "&7┃ &e&lPVP &r&7┃"));
    }

    public void sendMessage(final CommandSender sender, final String key, final String[] args) {
        final ConfigMessage message = getMessage(key);

        if (message != null) message.send(sender, args == null ? new String[]{} : args);
    }

    public void sendHelpMessage(final CommandSender sender, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (args.length > 0) {
                try {
                    final int page = Integer.parseInt(args[0]);

                    if (page < 3 && page > 0 && PermissionsHandler.INSTANCE.hasPermission(player, "help-admin-commands")) {
                        player.spigot().sendMessage(helpMessage[1]);
                        return;
                    }
                } catch (final NumberFormatException ignored) {}
            }

            player.spigot().sendMessage(helpMessage[0]);
            return;
        }

        sender.sendMessage("/reload is the only command possible on console");
    }

    public String getPrefix() {
        return prefix;
    }

    private ConfigMessage getMessage(final String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        final Configuration section = messages.getSection(key);

        if (section == null) return null;

        final ConfigMessage message = new ConfigMessage(section);

        cache.put(key, message);

        return message;
    }

}
