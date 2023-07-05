package net.jandie1505.musicbot.console.commands;

import net.dv8tion.jda.api.JDA;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.CommandExecutor;

import java.util.List;

public class ShardsCommand implements CommandExecutor {
    private final MusicBot musicBot;

    public ShardsCommand(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public String onCommand(String command, String[] args) {
        try {

            if (this.musicBot.getShardManager() == null) {
                return "Bot is currently shut down";
            }

            if (args.length == 0) {
                return "Shards command help:\n" +
                        "shards info" +
                        "shards list\n" +
                        "shards start <id>\n" +
                        "stards stop <id>\n" +
                        "shards restart <id>\n" +
                        "shards start-all\n" +
                        "shards stop-all\n" +
                        "shards restart-all";
            }

            switch (args[0]) {
                case "info" -> {
                    return "Shards queued: " + this.musicBot.getShardManager().getShardsQueued() + "\n" +
                            "Shards running: " + this.musicBot.getShardManager().getShardsRunning() + "\n" +
                            "Shards total: " + this.musicBot.getShardManager().getShardsTotal();
                }
                case "list" -> {
                    String output = "| SHARD ID | STATUS | GUILDS |";

                    for (JDA jda : List.copyOf(this.musicBot.getShardManager().getShards())) {
                        output = output + "\n| " + jda.getShardInfo().getShardId() + " | " + jda.getStatus() + " | " + jda.getGuilds().size() + " |";
                    }

                    return output;
                }
                case "start" -> {

                    if (args.length < 2) {
                        return "Usage: shards start <id>";
                    }

                    this.musicBot.startShard(Integer.parseInt(args[1]));

                    return "Sent start shard command";
                }
                case "stop" -> {

                    if (args.length < 2) {
                        return "Usage: shards stop <id>";
                    }

                    this.musicBot.stopShard(Integer.parseInt(args[1]));

                    return "Sent stop shard command";
                }
                case "restart" -> {

                    if (args.length < 2) {
                        return "Usage: shards restart <id>";
                    }

                    this.musicBot.restartShard(Integer.parseInt(args[1]));

                    return "Sent restart shard command";
                }
                case "start-all" -> {
                    this.musicBot.startShards();
                    return "Sent start-all shard command";
                }
                case "stop-all" -> {
                    this.musicBot.stopShards();
                    return "Sent stop-all shard command";
                }
                case "restart-all" -> {
                    this.musicBot.restartShards();
                    return "Sent restart-all shard command";
                }
                default -> {
                    return "Unknown subcommand. Run command without a subcommand for help.";
                }
            }

        } catch (IllegalArgumentException e) {
            return "Illegal argument";
        }
    }
}
