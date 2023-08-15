package net.jandie1505.musicbot.console.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.CommandExecutor;
import net.jandie1505.musicbot.utilities.BotStatus;

import java.util.ArrayList;
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
                        "guild leave <id>\n" +
                        "guild delete-commands <id>/all\n" +
                        "guild setup-commands <id>/all\n" +
                        "guild get-commands <id>";
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
                case "delete-commands" -> {

                    if (args.length < 2) {
                        return "Usage: guild delete-commands <id>/all";
                    }

                    List<Guild> guilds = new ArrayList<>();

                    if (args[1].equalsIgnoreCase("all")) {
                        guilds.addAll(this.musicBot.getShardManager().getGuilds());
                    } else {
                        Guild g = this.musicBot.getShardManager().getGuildById(Long.parseLong(args[1]));

                        if (g == null) {
                            return "Guild does not exist";
                        }

                        guilds.add(g);
                    }

                    for (Guild g : guilds) {
                        this.musicBot.getGMS().deleteCommands(g);
                    }

                    return "Delete of guild slash commands started";
                }
                case "setup-commands" -> {

                    if (args.length < 2) {
                        return "Usage: guild setup-commands <id>/all";
                    }

                    List<Guild> guilds = new ArrayList<>();

                    if (args[1].equalsIgnoreCase("all")) {
                        guilds.addAll(this.musicBot.getShardManager().getGuilds());
                    } else {
                        Guild g = this.musicBot.getShardManager().getGuildById(Long.parseLong(args[1]));

                        if (g == null) {
                            return "Guild does not exist";
                        }

                        guilds.add(g);
                    }

                    for (Guild g : guilds) {
                        this.musicBot.getGMS().setupCommands(g);
                    }

                    return "Setup of guild slash commands started";
                }
                case "get-commands" -> {

                    if (args.length < 2) {
                        return "Usage: guild get-commands <id>";
                    }

                    Guild g = this.musicBot.getShardManager().getGuildById(Long.parseLong(args[1]));

                    if (g == null) {
                        return "Guild does not exist";
                    }

                    List<Command> commands = g.retrieveCommands().complete();

                    String response = "COMMANDS:";

                    for (Command cmd : commands) {

                        if (cmd.getApplicationIdLong() != cmd.getJDA().getSelfUser().getIdLong()) {
                            continue;
                        }

                        response = response + "\n" + cmd.getIdLong() + " " + cmd.getName() + ";";

                    }

                    return response;
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
