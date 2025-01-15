package pebbleprojects.strongMCPvP.handlers.discord;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import pebbleprojects.strongMCPvP.functions.profile.Profile;
import pebbleprojects.strongMCPvP.functions.discord.Embed;
import pebbleprojects.strongMCPvP.functions.discord.TextMessage;
import pebbleprojects.strongMCPvP.functions.profile.RealProfile;
import pebbleprojects.strongMCPvP.handlers.DataHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileHandler {

    private final List<Embed> embeds;
    public static ProfileHandler INSTANCE;
    private final List<TextMessage> messages;
    private boolean sendThinking, privateReply;
    private final Map<String, Profile> profiles;
    private final Map<String, RealProfile> realProfiles;

    public ProfileHandler() {
        DataHandler.INSTANCE.getLogger().info("Loading Discord Profile Handler...");

        INSTANCE = this;

        embeds = new ArrayList<>();
        messages = new ArrayList<>();

        profiles = new ConcurrentHashMap<>();
        realProfiles = new ConcurrentHashMap<>();

        update();

        DataHandler.INSTANCE.getLogger().info("Loading Database Handler...");
    }

    public void update() {
        embeds.clear();

        sendThinking = DiscordHandler.INSTANCE.getDiscord().getBoolean("commands.profile.thinking", true);
        privateReply = DiscordHandler.INSTANCE.getDiscord().getBoolean("commands.profile.private", false);

        embeds.add(new Embed(DiscordHandler.INSTANCE.getDiscord().getSection("commands.profile.success")));
        embeds.add(new Embed(DiscordHandler.INSTANCE.getDiscord().getSection("commands.profile.failed.not-found")));
        embeds.add(new Embed(DiscordHandler.INSTANCE.getDiscord().getSection("commands.profile.failed.error")));

        messages.add(new TextMessage(DiscordHandler.INSTANCE.getDiscord().getSection("commands.profile.success")));
        messages.add(new TextMessage(DiscordHandler.INSTANCE.getDiscord().getSection("commands.profile.failed.not-found")));
        messages.add(new TextMessage(DiscordHandler.INSTANCE.getDiscord().getSection("commands.profile.failed.error")));
    }

    public Profile getProfile(final String query) {
        final String lowercaseQuery = query.toLowerCase();

        if (profiles.containsKey(lowercaseQuery)) {
            return profiles.get(lowercaseQuery);
        }

        final Profile profile = new Profile(query);
        profiles.put(query, profile);

        return profile;
    }

    public void replyToInteraction(final SlashCommandInteractionEvent event) {
        if (sendThinking) {
            event.deferReply().setEphemeral(privateReply).queue();
        }

        final Profile profile = getProfile(Objects.requireNonNull(event.getOption("player", OptionMapping::getAsString)));

        profile.loadWithQuery();

        if (profile.getDeaths().equals("-1") || profile.getKills().equals("-1") || profile.getSouls(true).equals("-1") || profile.getPoints(true).equals("-1")) {
            failed(event, profile);
            return;
        }

        success(event, profile);
    }

    public RealProfile getRealProfile(final String name) {
        final String query = name.toLowerCase();

        if (realProfiles.containsKey(query)) {
            return realProfiles.get(query);
        }

        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + query).openConnection();

            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                final StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                connection.disconnect();

                final String s = content.toString();
                if (s.contains("\"id\"") && s.contains("\"name\"")) {
                    final RealProfile realProfile = new RealProfile(formatAndConvertUUID(s.split("id\" : \"")[1].split("\"")[0]),
                            s.split("name\" : \"")[1].split("\"")[0]);

                    realProfiles.put(query, realProfile);

                    return realProfile;
                }
            }
            return new RealProfile(null, name);
        } catch (final IOException ignored) {
            return new RealProfile(null, name);
        }
    }

    private UUID formatAndConvertUUID(String rawUuid) {
        rawUuid = rawUuid.replace("-", "");

        if (rawUuid.length() != 32)
            throw new IllegalArgumentException("Invalid UUID string length.");

        return UUID.fromString(rawUuid.substring(0, 8) + "-" +
                rawUuid.substring(8, 12) + "-" +
                rawUuid.substring(12, 16) + "-" +
                rawUuid.substring(16, 20) + "-" +
                rawUuid.substring(20));
    }

    private void failed(final SlashCommandInteractionEvent event, final Profile profile) {
        final Embed embed = profile.getError() != null ? embeds.get(2) : embeds.get(1);
        final TextMessage textMessage = profile.getError() != null ? messages.get(2) : messages.get(1);

        reply(event, profile, embed, textMessage);
    }

    private void success(final SlashCommandInteractionEvent event, final Profile profile) {
        final Embed embed = embeds.get(0);
        final TextMessage textMessage = messages.get(0);

        reply(event, profile, embed, textMessage);
    }

    private void reply(final SlashCommandInteractionEvent event, final Profile profile, final Embed embed, final TextMessage textMessage) {
        final String text = textMessage.getText(profile);
        final MessageEmbed messageEmbed = embed.getEmbed(profile);

        if (text != null) {
            if (messageEmbed != null) {
                if (sendThinking) {
                    event.getHook().sendMessage(text).setEmbeds(messageEmbed).queue();
                    return;
                }

                event.reply(text).setEmbeds(messageEmbed).setEphemeral(privateReply).queue();
                return;
            }

            if (sendThinking) {
                event.getHook().sendMessage(text).queue();
                return;
            }

            event.reply(text).setEphemeral(privateReply).queue();
            return;
        }

        if (messageEmbed != null) {
            if (sendThinking) {
                event.getHook().sendMessageEmbeds(messageEmbed).queue();
                return;
            }

            event.replyEmbeds(messageEmbed).setEphemeral(privateReply).queue();
        }
    }
}
