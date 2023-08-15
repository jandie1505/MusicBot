package net.jandie1505.musicbot.console.commands;

import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.CommandExecutor;

public class BotCommand implements CommandExecutor {
    private final MusicBot musicBot;

    public BotCommand(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public String onCommand(String command, String[] args) {

        if (args.length == 0) {
            return "Bot command help:\n" +
                    "bot status\n" +
                    "bot stop\n" +
                    "bot start [shardsTotal]";
        }

        switch (args[0]) {
            case "status" -> {
                return "Current bot status: " + this.musicBot.getBotStatus();
            }
            case "stop" -> {
                this.musicBot.shutdownShardManager();
                return "Requested bot shutdown";
            }
            case "start" -> {

                if (args.length > 1) {
                    try {
                        this.musicBot.startShardManager(Integer.parseInt(args[1]));
                    } catch (IllegalArgumentException e) {
                        return "Please specify a valid int value for the shards count";
                    }
                } else {
                    this.musicBot.startShardManager();
                }

                return "Requested bot start";
            }
            default -> {
                return "Unknown subcommand";
            }
        }

    }
}
