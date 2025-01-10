package pebbleprojects.strongMCPvP.handlers;

import pebbleprojects.strongMCPvP.functions.Quest;

import java.util.ArrayList;
import java.util.List;

public class QuestHandler {

    private final List<Quest> quests;
    public static QuestHandler INSTANCE;

    public QuestHandler() {
        INSTANCE = this;

        quests = new ArrayList<>();
    }

    public void update() {
        quests.clear();
    }

}
