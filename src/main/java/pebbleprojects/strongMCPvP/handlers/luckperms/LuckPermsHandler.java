package pebbleprojects.strongMCPvP.handlers.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import pebbleprojects.strongMCPvP.handlers.DataHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class LuckPermsHandler {

    private final LuckPerms api;
    public static LuckPermsHandler INSTANCE;
    private final Map<UUID, CachedUser> userCache;
    private final ScheduledExecutorService cleanupService;

    private static class CachedUser {
        private final User user;
        private final long timestamp;

        private CachedUser(final User user) {
            this.user = user;
            this.timestamp = System.currentTimeMillis();
        }

        private boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 60000;
        }
    }

    public LuckPermsHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading LuckPerms Handler...");

        INSTANCE = this;
        userCache = new ConcurrentHashMap<>();

        final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();
            DataHandler.INSTANCE.getLogger().info("Found LuckPerms and connected successfully!");
        } else {
            api = null;
            DataHandler.INSTANCE.getLogger().warning("Failed to connect to LuckPerms!");
        }

        this.cleanupService = Executors.newSingleThreadScheduledExecutor();
        cleanupService.scheduleAtFixedRate(this::cleanupCache, 1, 1, TimeUnit.MINUTES);

        DataHandler.INSTANCE.getLogger().info("Loaded LuckPerms Handler with caching enabled!");
    }

    private void cleanupCache() {
        userCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
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

    public void shutdown() {
        userCache.clear();
        cleanupService.shutdown();
        try {
            cleanupService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}