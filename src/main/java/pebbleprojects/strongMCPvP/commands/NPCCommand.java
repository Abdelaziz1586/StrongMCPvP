package pebbleprojects.strongMCPvP.commands;

import pebbleprojects.strongMCPvP.handlers.MessageHandler;
import pebbleprojects.strongMCPvP.handlers.NPCHandler;
import pebbleprojects.strongMCPvP.handlers.PermissionsHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NPCCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        TaskHandler.INSTANCE.runAsync(() -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("/reload is the only command possible on console");
                return;
            }

            final Player player = (Player) sender;

            if (args.length == 0) {
                handleGuiCommand(player);
                return;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    create(player, args);
                    break;
                case "remove":
                    remove(player, args);
                    break;
                default:
                    MessageHandler.INSTANCE.sendMessage(player, "npc.others.invalid-arguments", null);
                    break;
            }
        });
        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (doesNotHaveCreatePermissions(player) && doesNotHaveRemovePermissions(player))
                return null;
        }

        if (args.length == 1) {
            return Stream.of("create", "remove")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return Stream.of("shop", "quests", "trails", "settings")
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }

    private void handleGuiCommand(final Player player) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.gui")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.command-no-permission", null);
            return;
        }

        NPCHandler.INSTANCE.openGUI(player);
    }

    private void create(final Player player, final String[] args) {
        if (doesNotHaveCreatePermissions(player)) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.others.command-no-permission", null);
            return;
        }

        if (args.length < 2) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.others.invalid-arguments", null);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "shop":
                handleCreateShop(player, args);
                break;
            case "quests":
                handleCreateQuests(player, args);
                break;
            case "trails":
                handleCreateTrails(player, args);
                break;
            case "settings":
                handleCreateSettings(player, args);
                break;
            default:
                MessageHandler.INSTANCE.sendMessage(player, "npc.others.invalid-arguments", null);
                break;
        }
    }

    private boolean doesNotHaveCreatePermissions(final Player player) {
        return !PermissionsHandler.INSTANCE.hasPermission(player, "npc.shop.create")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "npc.quests.create")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "npc.trails.create")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "npc.settings.create");
    }

    private boolean doesNotHaveRemovePermissions(final Player player) {
        return !PermissionsHandler.INSTANCE.hasPermission(player, "npc.shop.remove")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "npc.quests.remove")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "npc.trails.remove")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "npc.settings.remove");
    }

    private void handleCreateShop(final Player player, final String[] args) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.shop.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.shop.create.failed.no-permission", null);
            return;
        }

        MessageHandler.INSTANCE.sendMessage(player, "npc.shop.create.success.creation.creating", null);
        NPCHandler.INSTANCE.createNPC(player.getLocation(), args.length > 2 ? args[2] : null, args.length > 3 ? args[3] : null, 0);
        MessageHandler.INSTANCE.sendMessage(player, "npc.shop.create.success.creation.created", null);
    }

    private void handleCreateQuests(final Player player, final String[] args) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.quests.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.quests.create.failed.no-permission", null);
            return;
        }

        MessageHandler.INSTANCE.sendMessage(player, "npc.quests.create.success.creation.creating", null);
        NPCHandler.INSTANCE.createNPC(player.getLocation(), args.length > 2 ? args[2] : null, args.length > 3 ? args[3] : null, 1);
        MessageHandler.INSTANCE.sendMessage(player, "npc.quests.create.success.creation.created", null);
    }

    private void handleCreateTrails(final Player player, final String[] args) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.trails.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.trails.create.failed.no-permission", null);
            return;
        }

        MessageHandler.INSTANCE.sendMessage(player, "npc.trails.create.success.creation.creating", null);
        NPCHandler.INSTANCE.createNPC(player.getLocation(), args.length > 2 ? args[2] : null, args.length > 3 ? args[3] : null, 2);
        MessageHandler.INSTANCE.sendMessage(player, "npc.trails.create.success.creation.created", null);
    }

    private void handleCreateSettings(final Player player, final String[] args) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.settings.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.settings.create.failed.no-permission", null);
            return;
        }

        MessageHandler.INSTANCE.sendMessage(player, "npc.settings.create.success.creation.creating", null);
        NPCHandler.INSTANCE.createNPC(player.getLocation(), args.length > 2 ? args[2] : null, args.length > 3 ? args[3] : null, 3);
        MessageHandler.INSTANCE.sendMessage(player, "npc.settings.create.success.creation.created", null);
    }

    private void remove(final Player player, final String[] args) {
        if (doesNotHaveCreatePermissions(player)) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.others.command-no-permission", null);
            return;
        }

        if (args.length < 3) {
            NPCHandler.INSTANCE.removeNPC(player);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "shop":
                handleRemoveShop(player, args);
                break;
            case "quests":
                handleRemoveQuests(player, args);
                break;
            case "trails":
                handleRemoveTrails(player, args);
                break;
            case "settings":
                handleRemoveSettings(player, args);
                break;
            default:
                MessageHandler.INSTANCE.sendMessage(player, "npc.others.invalid-arguments", null);
                break;
        }
    }

    private void handleRemoveShop(final Player player, final String[] args) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.shop.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.shop.create.failed.no-permission", null);
            return;
        }

        try {
            if (NPCHandler.INSTANCE.removeShopNPC(Integer.parseInt(args[2]))) {
                MessageHandler.INSTANCE.sendMessage(player, "npc.shop.remove.success", new String[]{"id," + args[2]});
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "npc.shop.remove.failed.invalid-id", null);
        } catch (final NumberFormatException ignored) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.shop.remove.failed.invalid-id", null);
        }
    }

    private void handleRemoveQuests(final Player player, final String[] args) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.quests.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.quests.create.failed.no-permission", null);
            return;
        }

        try {
            if (NPCHandler.INSTANCE.removeQuestsNPC(Integer.parseInt(args[2]))) {
                MessageHandler.INSTANCE.sendMessage(player, "npc.quests.remove.success", new String[]{"id," + args[2]});
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "npc.quests.remove.failed.invalid-id", null);
        } catch (final NumberFormatException ignored) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.quests.remove.failed.invalid-id", null);
        }
    }

    private void handleRemoveTrails(final Player player, final String[] args) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.trails.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.trails.create.failed.no-permission", null);
            return;
        }

        try {
            if (NPCHandler.INSTANCE.removeTrailsNPC(Integer.parseInt(args[2]))) {
                MessageHandler.INSTANCE.sendMessage(player, "npc.trails.remove.success", new String[]{"id," + args[2]});
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "npc.trails.remove.failed.invalid-id", null);
        } catch (final NumberFormatException ignored) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.trails.remove.failed.invalid-id", null);
        }
    }

    private void handleRemoveSettings(final Player player, final String[] args) {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "npc.settings.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.settings.create.failed.no-permission", null);
            return;
        }

        try {
            if (NPCHandler.INSTANCE.removeSettingsNPC(Integer.parseInt(args[2]))) {
                MessageHandler.INSTANCE.sendMessage(player, "npc.settings.remove.success", new String[]{"id," + args[2]});
                return;
            }

            MessageHandler.INSTANCE.sendMessage(player, "npc.settings.remove.failed.invalid-id", null);
        } catch (final NumberFormatException ignored) {
            MessageHandler.INSTANCE.sendMessage(player, "npc.settings.remove.failed.invalid-id", null);
        }
    }
}
