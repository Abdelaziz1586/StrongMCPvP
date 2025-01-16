package pebbleprojects.strongMCPvP.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.handlers.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LeaderboardCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        TaskHandler.INSTANCE.runAsync(() -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("/reload is the only command possible on console");
                return;
            }

            final Player player = (Player) sender;

            if (args.length < 2) {
                MessageHandler.INSTANCE.sendMessage(player, "leaderboard.others.invalid-arguments", null);
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
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.others.invalid-arguments", null);
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
            return Stream.of("kills", "deaths", "assists", "points", "souls")
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }

    private boolean doesNotHaveCreatePermissions(final Player player) {
        return !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.kills.create")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.deaths.create")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.assists.create")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.souls.create")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.points.create");
    }

    private boolean doesNotHaveRemovePermissions(final Player player) {
        return !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.kills.remove")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.deaths.remove")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.assists.remove")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.souls.remove")
                && !PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.points.remove");
    }

    private void create(final Player player, final String[] args) {
        if (doesNotHaveCreatePermissions(player)) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.others.command-no-permission", null);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "kills":
                try {
                    handleCreateKills(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.kills.create.failed.error", null);
                }
                break;
            case "deaths":
                try {
                    handleCreateDeaths(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.deaths.create.failed.error", null);
                }
                break;
            case "assists":
                try {
                    handleCreateAssists(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.assists.create.failed.error", null);
                }
                break;
            case "souls":
                try {
                    handleCreateSouls(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.souls.create.failed.error", null);
                }
                break;
            case "points":
                try {
                    handleCreatePoints(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.points.create.failed.error", null);
                }
                break;
            default:
                MessageHandler.INSTANCE.sendMessage(player, "leaderboard.others.invalid-arguments", null);
                break;
        }
    }

    private void handleCreateKills(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.kills.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.kills.create.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.createLeaderboard(player.getLocation(), "kills");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.kills.create.success", null);
    }

    private void handleCreateDeaths(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.deaths.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.deaths.create.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.createLeaderboard(player.getLocation(), "deaths");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.deaths.create.success", null);
    }

    private void handleCreateAssists(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.assists.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.assists.create.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.createLeaderboard(player.getLocation(), "assists");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.assists.create.success", null);
    }

    private void handleCreateSouls(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.souls.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.souls.create.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.createLeaderboard(player.getLocation(), "souls");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.souls.create.success", null);
    }

    private void handleCreatePoints(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.points.create")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.points.create.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.createLeaderboard(player.getLocation(), "points");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.points.create.success", null);
    }

    private void remove(final Player player, final String[] args) {
        if (doesNotHaveRemovePermissions(player)) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.others.command-no-permission", null);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "kills":
                try {
                    handleRemoveKills(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.kills.remove.failed.error", null);
                }
                break;
            case "deaths":
                try {
                    handleRemoveDeaths(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.deaths.remove.failed.error", null);
                }
                break;
            case "assists":
                try {
                    handleRemoveAssists(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.assists.remove.failed.error", null);
                }
                break;
            case "souls":
                try {
                    handleRemoveSouls(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.souls.remove.failed.error", null);
                }
                break;
            case "points":
                try {
                    handleRemovePoints(player);
                } catch (final SQLException ignored) {
                    MessageHandler.INSTANCE.sendMessage(player, "leaderboard.points.remove.failed.error", null);
                }
                break;
            default:
                MessageHandler.INSTANCE.sendMessage(player, "leaderboard.others.invalid-arguments", null);
                break;
        }
    }

    private void handleRemoveKills(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.kills.remove")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.kills.remove.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.removeLeaderboard("kills");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.kills.remove.success", null);
    }

    private void handleRemoveDeaths(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.deaths.remove")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.deaths.remove.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.removeLeaderboard("deaths");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.deaths.remove.success", null);
    }

    private void handleRemoveAssists(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.assists.remove")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.assists.remove.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.removeLeaderboard("assists");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.assists.remove.success", null);
    }

    private void handleRemoveSouls(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.souls.remove")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.souls.remove.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.removeLeaderboard("souls");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.souls.remove.success", null);
    }

    private void handleRemovePoints(final Player player) throws SQLException {
        if (!PermissionsHandler.INSTANCE.hasPermission(player, "leaderboard.points.remove")) {
            MessageHandler.INSTANCE.sendMessage(player, "leaderboard.points.remove.failed.no-permission", null);
            return;
        }

        LeaderboardHandler.INSTANCE.removeLeaderboard("points");
        MessageHandler.INSTANCE.sendMessage(player, "leaderboard.points.remove.success", null);
    }
}
