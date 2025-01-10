package pebbleprojects.strongMCPvP.functions.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import pebbleprojects.strongMCPvP.functions.Profile;
import pebbleprojects.strongMCPvP.functions.config.Configuration;
import pebbleprojects.strongMCPvP.handlers.DataHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public final class Embed {

    private Color color;
    private Title title;
    private int MAX_COLOR;
    private Author author;
    private Footer footer;
    private List<Field> fields;
    private String description;
    private String image, thumbnail;
    private boolean random, common, timestamp;

    public Embed(Configuration section) {
        if (section == null || !section.getBoolean("embed.enabled", true)) return;

        section = section.getSection("embed");

        MAX_COLOR = 1 << 24;

        fields = new ArrayList<>();

        if (section.getBoolean("author.enabled", true)) {
            final String url = section.getBoolean("author.url.enabled", false) ? section.getString("author.url.url", "") : null,
                    icon = section.getBoolean("author.icon.enabled", false) ? section.getString("author.icon.url", "") : null;

            author = new Author(section.getString("author.text", null), url, icon);
        }

        if (section.getBoolean("title.enabled", true)) {
            final String url = section.getBoolean("title.url.enabled", false) ? section.getString("title.url.url", "") : null;

            title = new Title(section.getString("title.text", null), url);
        }

        if (section.getBoolean("description.enabled", false)) {
            description = section.getString("description.text", null);
        }

        final Configuration colorSection = section.getSection("color");

        if (colorSection != null) {
            if (!colorSection.getBoolean("random") && !colorSection.getBoolean("common-color")) {
                random = false;
                common = false;
                try {
                    final String[] s = colorSection.getString("rgb-color", "119, 128, 139").replace(" ", "").split(",");

                    if (s.length != 3) {
                        DataHandler.INSTANCE.getLogger().severe("Couldn't fetch, split, or cast embed RGB color! Automatically setting it to \"119, 128, 139\"");
                        color = new Color(119, 128, 139);
                    } else {
                        color = new Color(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
                    }
                } catch (final Exception ignored) {
                    DataHandler.INSTANCE.getLogger().severe("Couldn't fetch, split, or cast embed RGB color! Automatically setting it to \"119, 128, 139\"");
                    color = new Color(119, 128, 139);
                }
            } else if (colorSection.getBoolean("random")) {
                random = true;
                common = false;
            } else {
                common = true;
                random = false;
            }
        } else {
            color = new Color(119, 128, 139);
        }

        if (section.getBoolean("fields.enabled", false)) {
            final Configuration fieldsSection = section.getSection("fields.embed-fields");

            if (fieldsSection == null) {
                DataHandler.INSTANCE.getLogger().severe("Couldn't fetch embed fields! Automatically disabled embed fields.");
            } else {
                Configuration fieldSection;
                for (final String key : fieldsSection.getKeys()) {
                    fieldSection = fieldsSection.getSection(key);

                    if (fieldSection == null) continue;

                    if (fieldSection.contains("name") && fieldSection.contains("value") && fieldSection.contains("inline")) {
                        fields.add(new Field(fieldSection.getString("name"), fieldSection.getString("value"), fieldSection.getBoolean("inline")));
                    }
                }
            }
        }

        timestamp = section.getBoolean("timestamp", true);

        if (section.getBoolean("footer.enabled", true)) {
            footer = new Footer(section.getString("footer.text", null), section.getBoolean("footer.icon.enabled") ? section.getString("footer.icon.url", null) : null);
        }

        image = section.getBoolean("image.enabled", true) ? section.getString("image.url", null) : null;

        thumbnail = section.getBoolean("thumbnail.enabled", true) ? section.getString("thumbnail.url", null) : null;
    }

    public MessageEmbed getEmbed(final Profile profile) {
        if (fields == null) return null;

        final EmbedBuilder embedBuilder = new EmbedBuilder();

        if (author != null) {
            embedBuilder.setAuthor(profile.replaceStringWithData(author.text, true), profile.replaceStringWithData(author.url, true), profile.replaceStringWithData(author.icon, true));
        }

        if (title != null) {
            embedBuilder.setTitle(profile.replaceStringWithData(title.text, true), profile.replaceStringWithData(title.url, true));
        }

        if (footer != null) {
            embedBuilder.setFooter(profile.replaceStringWithData(footer.text, true), profile.replaceStringWithData(footer.icon, true));
        }

        for (final Field field : fields) {
            embedBuilder.addField(profile.replaceStringWithData(field.name, true), profile.replaceStringWithData(field.value, true), field.inline);
        }

        if (random) {
            embedBuilder.setColor(generateRandomColor());
        } else if (common) {
            try {
                embedBuilder.setColor(getCommonColor("https://mc-heads.net/player/" + profile.getQuery()));
            } catch (final IOException ignored) {
                embedBuilder.setColor(new Color(119, 128, 139));
            }
        } else {
            embedBuilder.setColor(color);
        }

        if (timestamp) {
            embedBuilder.setTimestamp(new Date().toInstant());
        }

        if (description != null) {
            embedBuilder.setDescription(profile.replaceStringWithData(description, true));
        }

        if (image != null) {
            embedBuilder.setImage(profile.replaceStringWithData(image, true));
        }

        if (thumbnail != null) {
            embedBuilder.setThumbnail(profile.replaceStringWithData(thumbnail, true));
        }

        return embedBuilder.build();
    }

    private Color generateRandomColor() {
        final Random random = new Random();

        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    private Color getCommonColor(final String imageUrl) throws IOException {
        final BufferedImage image = ImageIO.read(new URL(imageUrl));

        final int[] colorCountArray = new int[MAX_COLOR];
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int[] rgbArray = new int[width * height];

        image.getRGB(0, 0, width, height, rgbArray, 0, width);

        IntStream.range(0, rgbArray.length).parallel().forEach(i -> {
            final int argb = rgbArray[i];
            final int alpha = (argb >> 24) & 0xff;
            if (alpha != 0) {
                final int rgb = argb & 0x00FFFFFF;
                colorCountArray[rgb]++;
            }
        });

        final int mostCommonColorRGB = IntStream.range(0, MAX_COLOR)
                .reduce((a, b) -> colorCountArray[a] > colorCountArray[b] ? a : b)
                .orElseThrow(() -> new RuntimeException("No color found in the image"));

        return new Color(mostCommonColorRGB);
    }

    private static class Author {
        final String text, url, icon;

        public Author(final String text, final String url, final String icon) {
            this.text = text;
            this.url = url;
            this.icon = icon;
        }
    }

    private static class Title {
        final String text, url;

        public Title(final String text, final String url) {
            this.text = text;
            this.url = url;
        }
    }

    private static class Field {
        final boolean inline;
        final String name, value;

        public Field(final String name, final String value, final boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }

    private static class Footer {
        private final String text, icon;

        private Footer(final String text, final String icon) {
            this.text = text;
            this.icon = icon;
        }
    }

}
