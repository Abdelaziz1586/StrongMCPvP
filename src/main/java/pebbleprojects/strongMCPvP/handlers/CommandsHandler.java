package pebbleprojects.strongMCPvP.handlers;

import pebbleprojects.strongMCPvP.PvP;
import pebbleprojects.strongMCPvP.commands.*;

public final class CommandsHandler {

    public CommandsHandler() {
        final PvP main = DataHandler.INSTANCE.getMain();

        main.getCommand("fix").setExecutor(new FixCommand());
        main.getCommand("npc").setExecutor(new NPCCommand());
        main.getCommand("help").setExecutor(new HelpCommand());
        main.getCommand("spawn").setExecutor(new SpawnCommand());
        main.getCommand("scramble").setExecutor(new ScrambleCommand());
        main.getCommand("spectate").setExecutor(new SpectateCommand());
        main.getCommand("settings").setExecutor(new SettingsCommand());
        main.getCommand("setspawn").setExecutor(new SetSpawnCommand());
        main.getCommand("leaderboard").setExecutor(new LeaderboardCommand());
    }

}
