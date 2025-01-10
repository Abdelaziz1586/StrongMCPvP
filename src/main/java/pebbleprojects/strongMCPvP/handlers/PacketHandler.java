package pebbleprojects.strongMCPvP.handlers;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayInWindowClick;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.customEvents.RightClickNPCEvent;
import pebbleprojects.strongMCPvP.functions.NPC;

import java.lang.reflect.Field;

public final class PacketHandler {

    public static PacketHandler INSTANCE;

    public PacketHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Packet Reader...");

        INSTANCE = this;

        TaskHandler.INSTANCE.runAsync(() -> {
            for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
                uninject(player);
                inject(player);
            }
        });

        DataHandler.INSTANCE.getLogger().info("Loaded Packet Reader!");

    }

    public void inject(final Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline().addAfter("decoder", player.getName(), new ChannelDuplexHandler() {
            @Override
            public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object packet) throws Exception {
                if (packet instanceof PacketPlayInWindowClick && GUIHandler.INSTANCE.onPacketInventoryClick(player, (PacketPlayInWindowClick) packet)) {
                    return;
                }

                if (packet instanceof PacketPlayInUseEntity) {
                    readPacket(player, (PacketPlayInUseEntity) packet);
                }

                super.channelRead(channelHandlerContext, packet);
            }
        });
    }

    public void uninject(final Player player) {
        final ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        if (pipeline.get(player.getName()) != null) {
            pipeline.remove(player.getName());
        }
    }

    private void readPacket(final Player player, final PacketPlayInUseEntity packetPlayInUseEntity) {
        if (packetPlayInUseEntity.a().name().equals("INTERACT")) {
            final int id = (int) getEntityID(packetPlayInUseEntity);
            for (final NPC npc : NPCHandler.INSTANCE.getNPCs()) {
                if (npc.getEntityPlayer().getId() == id) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DataHandler.INSTANCE.getMain(), () -> Bukkit.getPluginManager().callEvent(new RightClickNPCEvent(player, npc)));
                    return;
                }
            }
        }
    }

    private Object getEntityID(final Object instance) {
        final Object o;
        try {
            final Field field = instance.getClass().getDeclaredField("a");

            field.setAccessible(true);
            o = field.get(instance);
            field.setAccessible(false);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return o;
    }
}
