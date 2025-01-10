package pebbleprojects.strongMCPvP.functions;

import dev.mqzen.boards.animation.core.Animation;
import dev.mqzen.boards.base.BoardAdapter;
import dev.mqzen.boards.base.Title;
import dev.mqzen.boards.body.Body;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import pebbleprojects.strongMCPvP.databaseData.*;
import pebbleprojects.strongMCPvP.handlers.KillStreakHandler;
import pebbleprojects.strongMCPvP.handlers.ScoreboardHandler;

import java.util.List;
import java.util.UUID;

public final class ScoreboardAdapter implements BoardAdapter {

    @Override
    public @NonNull Title title(final Player player) {
        final String title = replaceStringWithData(player, ScoreboardHandler.INSTANCE.getTitle());

        return Title.builder()
                .withText(title)
                .withAnimation(new Animation<>(title, title))
                .build();
    }

    @Override
    public @NonNull Body getBody(final Player player) {
        final List<String> lines = ScoreboardHandler.INSTANCE.getLines();

        lines.replaceAll(line -> replaceStringWithData(player, line));

        return Body.of(lines);
    }

    private String replaceStringWithData(final Player player, final String string) {
        final UUID uuid = player.getUniqueId();
        return string
                .replace("%player%", player.getName())
                .replace("%souls%", String.valueOf(Souls.INSTANCE.get(uuid)))
                .replace("%kills%", String.valueOf(Kills.INSTANCE.get(uuid)))
                .replace("%deaths%", String.valueOf(Deaths.INSTANCE.get(uuid)))
                .replace("%assists%", String.valueOf(Assists.INSTANCE.get(uuid)))
                .replace("%points%", String.valueOf(Points.INSTANCE.get(uuid)))
                .replace("%killStreak%", String.valueOf(KillStreakHandler.INSTANCE.getKillStreaks(uuid)));
    }
}
