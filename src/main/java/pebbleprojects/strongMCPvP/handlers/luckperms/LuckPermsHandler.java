package pebbleprojects.strongMCPvP.handlers.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import pebbleprojects.strongMCPvP.handlers.DataHandler;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class LuckPermsHandler {

    private final LuckPerms api;
    public static LuckPermsHandler INSTANCE;

    public LuckPermsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading LuckPerms Handler...");

        INSTANCE = this;

        final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();

            DataHandler.INSTANCE.getLogger().info("Found LuckPerms and connected successfully!");
        } else {
            api = null;
        }

        DataHandler.INSTANCE.getLogger().info("Loaded LuckPerms Handler!");
    }

    public String translateMessage(final UUID uuid, final String text) {
        try {
            final User user = api.getUserManager().loadUser(uuid).get();
            if (user == null) return text;

            final CachedMetaData metaData = user.getCachedData().getMetaData();
            final String prefix = metaData.getPrefix(),
                    suffix = metaData.getSuffix();

            return text
                    .replace("%luckperms_prefix%", prefix == null ? "" : prefix)
                    .replace("%luckperms_suffix%", suffix == null ? "" : suffix);
        } catch (final InterruptedException | ExecutionException e) {
            return text;
        }
    }

}
