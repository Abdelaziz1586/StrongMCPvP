package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.command.CommandSender;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class PermissionsHandler {

    private Configuration permissions;
    private final File permissionsFile;
    public static PermissionsHandler INSTANCE;

    public PermissionsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Permissions Handler...");

        INSTANCE = this;

        permissionsFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "permissions.yml");

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Permissions Handler!");
    }

    public void update() {
        if (!permissionsFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("permissions.yml", permissionsFile);
        }

        try {
            permissions = ConfigurationProvider.getProvider(YamlConfiguration.class).load(permissionsFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load permissions.yml in memory: " + e.getMessage());
        }
    }

    public boolean hasPermission(final CommandSender sender, final String permissionKey) {
        return sender.isOp() || permissions.contains(permissionKey) && sender.hasPermission(permissions.getString(permissionKey));
    }

}
