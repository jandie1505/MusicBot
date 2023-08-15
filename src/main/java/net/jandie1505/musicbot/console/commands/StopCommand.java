package net.jandie1505.musicbot.console.commands;

import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.CommandExecutor;

public class StopCommand implements CommandExecutor {
    private final MusicBot musicBot;

    public StopCommand(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public String onCommand(String command, String[] args) {
        this.musicBot.shutdown();
        return "Shutting down...";
    }
}
