package pebbleprojects.strongMCPvP.handlers.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import pebbleprojects.strongMCPvP.handlers.DataHandler;

import java.util.UUID;

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
            DataHandler.INSTANCE.getLogger().warning("Failed to connect to LuckPerms!");
        }

        DataHandler.INSTANCE.getLogger().info("Loaded LuckPerms Handler with caching enabled!");
    }

    public String translateMessage(final UUID uuid, final String text) {
        if (api == null) return text;

        User user = api.getUserManager().getUser(uuid);
        if (user == null)
            user = api.getUserManager().loadUser(uuid).join();

        return user == null ? text : formatMessage(user, text);
    }

    private String formatMessage(final User user, final String text) {
        final CachedMetaData metaData = user.getCachedData().getMetaData();
        final String prefix = metaData.getPrefix(),
                suffix = metaData.getSuffix();

        return text
                .replace("%luckperms_prefix%", prefix == null ? "" : prefix)
                .replace("%luckperms_suffix%", suffix == null ? "" : suffix);
    }
}