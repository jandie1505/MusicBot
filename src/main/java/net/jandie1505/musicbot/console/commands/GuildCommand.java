package net.jandie1505.musicbot.console.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.CommandExecutor;
import net.jandie1505.musicbot.utilities.BotStatus;

import java.util.List;

public class GuildCommand implements CommandExecutor {
    private final MusicBot musicBot;

    public GuildCommand(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public String onCommand(String command, String[] args) {
        try {

            if (this.musicBot.getBotStatus() != BotStatus.ACTIVE) {
                return "Command disabled because bot is not started";
            }

            if (args.length == 0) {
                return "Guild command help:\n" +
                        "guild list\n" +
                        "guild leave <id>";
            }

            switch (args[0]) {
                case "list" -> {
                    String output = "| GUILD ID | OWNER | MEMBERS |";

                    for (Guild g : List.copyOf(this.musicBot.getShardManager().getGuilds())) {
                        Member owner = g.retrieveOwner().complete();

                        output = output + "\n| " + g.getId() + " | " + owner.getUser().getName() + "#" + owner.getUser().getDiscriminator() + " (" + owner.getUser().getIdLong() + ") | " + g.getMemberCount() + " |";
                    }

                    if (!this.musicBot.completeOnline()) {
                        output = output + "\nSome guilds might not be listed because not all shards are online";
                    }

                    return output;
                }
                case "leave" -> {

                    if (args.length < 2) {
                        return "Usage: guild leave <id>";
                    }

                    Guild g = this.musicBot.getShardManager().getGuildById(Long.parseLong(args[1]));

                    if (g == null) {
                        return "Guild does not exist";
                    }

                    g.leave().queue();
                    return "Guild leave queued";
                }
                default -> {
                    return "Unknown subcommand. Run command without any arguments for help.";
                }
            }

        } catch (IllegalArgumentException e) {
            return "Illegal argument";
        }
    }
}
