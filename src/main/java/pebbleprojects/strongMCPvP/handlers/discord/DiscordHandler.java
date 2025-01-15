package pebbleprojects.strongMCPvP.handlers.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.functions.config.ConfigurationProvider;
import pebbleprojects.strongMCPvP.functions.config.YamlConfiguration;
import pebbleprojects.strongMCPvP.handlers.DataHandler;
import pebbleprojects.strongMCPvP.handlers.TaskHandler;

import java.io.File;
import java.io.IOException;

public final class DiscordHandler extends ListenerAdapter {

    private JDA jda;
    private Configuration discord;
    private final File discordFile;
    public static DiscordHandler INSTANCE;

    public DiscordHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Discord Handler...");

        INSTANCE = this;

        discordFile = new File(DataHandler.INSTANCE.getDataFolder().getPath(), "discord.yml");

        update();

        DataHandler.INSTANCE.getLogger().info("Loaded Discord Handler!");
    }

    public void update() {
        if (!discordFile.exists()) {
            DataHandler.INSTANCE.copyToPluginDirectory("discord.yml", discordFile);
        }

        try {
            discord = ConfigurationProvider.getProvider(YamlConfiguration.class).load(discordFile);
        } catch (final IOException e) {
            DataHandler.INSTANCE.getLogger().warning("Unable to load discord.yml in memory: " + e.getMessage());
        }

        TaskHandler.INSTANCE.runAsync(() -> {

            if (discord.getBoolean("enabled", false)) {
                DataHandler.INSTANCE.getLogger().info("Attempting to load discord bot... (ASYNC)");

                JDALogger.setFallbackLoggerEnabled(false);

                if (jda != null) {
                    jda.shutdown();
                }

                try {
                    jda = JDABuilder.createDefault(discord.getString("token", null))
                            .build()
                            .awaitReady();

                    DataHandler.INSTANCE.getLogger().info("Loaded discord bot!");

                    DataHandler.INSTANCE.getLogger().info("Registering discord bot commands...");
                    jda.updateCommands().addCommands(
                            Commands.slash("profile", "Get profile of a player")
                                    .setGuildOnly(true)
                                    .addOption(OptionType.STRING, "player", "Player IGN", true)
                    ).queue();
                    DataHandler.INSTANCE.getLogger().info("Registered discord bot commands!");

                    DataHandler.INSTANCE.getLogger().info("Registering discord bot events...");
                    jda.addEventListener(this);
                    DataHandler.INSTANCE.getLogger().info("Registered discord bot events!");
                } catch (final InvalidTokenException ignored) {
                    DataHandler.INSTANCE.getLogger().severe("Invalid discord bot token, disabling bot option until next reload.");
                    jda = null;
                } catch (final InterruptedException e) {
                    DataHandler.INSTANCE.getLogger().severe("Failed to load discord bot, disabling bot option until next reload. Error: " + e.getMessage());
                    jda = null;
                }
            }
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        TaskHandler.INSTANCE.runAsync(() -> ProfileHandler.INSTANCE.replyToInteraction(event));
    }

    public Configuration getDiscord() {
        return discord;
    }

    public void shutdown() {
        if (jda != null) jda.shutdown();
    }
}
