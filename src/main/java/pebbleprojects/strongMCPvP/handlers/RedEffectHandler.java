package pebbleprojects.strongMCPvP.handlers;

import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pebbleprojects.strongMCPvP.databaseData.RedEffect;

public final class RedEffectHandler {

    public RedEffectHandler() {
        TaskHandler.INSTANCE.runTaskTimerAsync(new BukkitRunnable() {
            @Override
            public void run() {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    checkAndApplyRedBorder(player);
                }
            }
        }, 10);
    }

    private void checkAndApplyRedBorder(final Player player) {
        if (RedEffect.INSTANCE.get(player.getUniqueId()) && player.getHealth() < 6.0) {
            applyRedBorderEffect(player);
            return;
        }

        resetRedBorder(player);
    }

    private void applyRedBorderEffect(final Player player) {
        final WorldBorder border = new WorldBorder();

        border.setCenter(player.getLocation().getX(), player.getLocation().getZ());
        border.setSize(20000000);
        border.setWarningTime(0);
        border.setWarningDistance(20000000);
        border.setDamageBuffer(0);
        border.setDamageAmount(0);

        sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

    private void resetRedBorder(final Player player) {
        final WorldBorder border = new WorldBorder();

        border.setCenter(player.getWorld().getSpawnLocation().getX(), player.getWorld().getSpawnLocation().getZ());
        border.setSize(60000000);

        sendPacket(player, new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

    private void sendPacket(final Player player, final PacketPlayOutWorldBorder packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
