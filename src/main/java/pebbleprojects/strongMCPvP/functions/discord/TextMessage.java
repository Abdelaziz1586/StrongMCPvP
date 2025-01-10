package pebbleprojects.strongMCPvP.functions.discord;

import pebbleprojects.strongMCPvP.functions.Profile;
import pebbleprojects.strongMCPvP.functions.config.Configuration;

public final class TextMessage {

    private String text;

    public TextMessage(Configuration section) {
        if (section == null || !section.getBoolean("text-message.enabled", false)) return;

        section = section.getSection("text-message");

        text = section.getString("text", null);
    }

    public String getText(final Profile profile) {
        return profile.replaceStringWithData(text, true);
    }

}
