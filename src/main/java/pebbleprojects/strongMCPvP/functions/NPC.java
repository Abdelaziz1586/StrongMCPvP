package pebbleprojects.strongMCPvP.functions;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public final class NPC {

    private final String name;
    private final int id, type;
    private final Location location;
    private final GameProfile gameProfile;
    private final EntityPlayer entityPlayer;
    private String textures, signature, skinName;

    public NPC(final int id, final int type, final String name, final String skinName, final Location location) {
        textures = null;
        signature = null;

        this.id = id;
        this.type = type;
        this.name = name;
        this.skinName = skinName;
        this.location = location;
        final WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        gameProfile = new GameProfile(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&', name));
        entityPlayer = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public void setSkin(final String skinName) {
        try {
            final HttpsURLConnection connection = (HttpsURLConnection) new URL("https://playerdb.co/api/player/minecraft/" + skinName).openConnection();

            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36");

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;

                final StringBuilder content = new StringBuilder();

                while ((inputLine = reader.readLine()) != null) {
                    content.append(inputLine);
                }

                reader.close();
                connection.disconnect();

                final String s = content.toString();

                textures = s.split("value\":\"")[1].split("\"")[0];
                signature = s.split("signature\":\"")[1].split("\"")[0];

                gameProfile.getProperties().put("textures", new Property("textures", textures, signature));
                this.skinName = skinName;
                return;
            }

            DataHandler.INSTANCE.getLogger().severe("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().severe("Connection could not be opened when fetching player skin (" + e.getMessage() + ")");
        }
    }

    public void setTexturesAndSignature(final String textures, final String signature) {
        this.textures = textures;
        this.signature = signature;
        entityPlayer.getProfile().getProperties().put("textures", new Property("textures", textures, signature));
    }

    public void sendShowPacket(final Player player) {
        final PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        final DataWatcher watcher = entityPlayer.getDataWatcher();
        watcher.watch(10, (byte) 127);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityPlayer.getId(), watcher, true));

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (entityPlayer.yaw * 256 / 360)));
        TaskHandler.INSTANCE.runAsync(() -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));
            }
        }, 5));
    }

    public void sendRemovePacket(final Player player) {
        (((CraftPlayer) player).getHandle().playerConnection).sendPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()));
    }

    public String getName() {
        return name;
    }

    public String getSkinName() {
        return skinName;
    }

    public String getTextures() {
        return textures;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    public String getSignature() {
        return signature;
    }

    public Location getLocation() {
        return location;
    }

}
