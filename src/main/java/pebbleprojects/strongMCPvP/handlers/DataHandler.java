package pebbleprojects.strongMCPvP.handlers;

import org.bukkit.command.CommandSender;
import pebbleprojects.strongMCPvP.PvP;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;
import pebbleprojects.strongMCPvP.handlers.discord.DiscordHandler;
import pebbleprojects.strongMCPvP.handlers.discord.ProfileHandler;
import pebbleprojects.strongMCPvP.handlers.papi.PlaceholderAPIHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

public final class DataHandler {

    private boolean reload;
    private final PvP main;
    private final Logger logger;
    public static DataHandler INSTANCE;
    private Configuration config, data;
    private final File dataFolder, dataFile, configFile;

    public DataHandler(final PvP main) {
        INSTANCE = this;

        reload = false;

        this.main = main;
        logger = main.getLogger();

        dataFolder = main.getDataFolder();

        if (dataFolder.mkdir()) {
            logger.info("Created plugin's directory!");
        }

        configFile = new File(dataFolder.getPath(), "config.yml");
        updateConfig();

        dataFile = new File(dataFolder.getPath(), "data.yml");
        updateData();

        new TaskHandler();
        new UtilsHandler();
        new PacketHandler();
        new LocationHandler();
        new PlaceholderAPIHandler();

        new GUIHandler();
        new NPCHandler();
        new KitsHandler();
        new ShopHandler();
        new GameHandler();
        new KillsHandler();
        new PerksHandler();
        new TrailsHandler();

        new QuestsHandler();
        new LevelsHandler();
        new DatabaseHandler();
        new MessageHandler();
        new SettingsHandler();
        new CommandsHandler();
        new RedEffectHandler();
        new ListenersHandler();
        new ChatInputHandler();
        new CombatLogHandler();
        new KillStreakHandler();
        new ScoreboardHandler();
        new PermissionsHandler();

        new DiscordHandler();
        new ProfileHandler();
    }

    public PvP getMain() {
        return main;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public Configuration getConfig() {
        return config;
    }

    public Configuration getData() {
        return data;
    }

    public void update(final CommandSender sender) {
        if (sender != null) {
            if (PermissionsHandler.INSTANCE.hasPermission(sender, "reload")) {
                if (reload) {
                    MessageHandler.INSTANCE.sendMessage(sender, "reload.already-reloading", null);
                    return;
                }

                reload = true;

                MessageHandler.INSTANCE.sendMessage(sender, "reload.reloading", null);
                update(null);
                MessageHandler.INSTANCE.sendMessage(sender, "reload.success", null);

                reload = false;
                return;
            }

            MessageHandler.INSTANCE.sendMessage(sender, "reload.no-permission", null);
            return;
        }

        if (dataFolder.mkdir()) {
            logger.info("Created plugin folder");
        }

        updateData();
        updateConfig();

        NPCHandler.INSTANCE.update();
        KitsHandler.INSTANCE.update();
        ShopHandler.INSTANCE.update();
        GameHandler.INSTANCE.update();
        KillsHandler.INSTANCE.update();
        PerksHandler.INSTANCE.update();
        TrailsHandler.INSTANCE.update();
        DiscordHandler.INSTANCE.update();
        ProfileHandler.INSTANCE.update();
        MessageHandler.INSTANCE.update();
        SettingsHandler.INSTANCE.update();
        DatabaseHandler.INSTANCE.update();
        CombatLogHandler.INSTANCE.update();
        KillStreakHandler.INSTANCE.update();
        ScoreboardHandler.INSTANCE.update();
        PermissionsHandler.INSTANCE.update();
    }

    public void saveData() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(data, dataFile);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyToPluginDirectory(final String name, final File file) {
        final InputStream inputStream = getResourceAsStream(name);

        if (inputStream != null) {
            try {
                Files.copy(inputStream, file.toPath());
                logger.info(name + " copied successfully.");
            } catch (final IOException e) {
                logger.warning("Failed to copy " + name + ": " + e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    logger.warning("Failed to close input stream: " + e.getMessage());
                }
            }
        } else {
            logger.warning("Unable to locate " + name + " in resources.");
        }
    }

    private InputStream getResourceAsStream(final String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    private void updateConfig() {
        if (!configFile.exists()) {
            copyToPluginDirectory("config.yml", configFile);
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (final IOException e) {
            logger.warning("Unable to load config.yml in memory: " + e.getMessage());
        }
    }

    private void updateData() {
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) {
                    logger.info("Created data.yml");
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            data = ConfigurationProvider.getProvider(YamlConfiguration.class).load(dataFile);
        } catch (final IOException e) {
            logger.warning("Unable to load data.yml in memory: " + e.getMessage());
        }
    }
}
