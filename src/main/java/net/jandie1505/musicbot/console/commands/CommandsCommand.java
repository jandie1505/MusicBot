package net.jandie1505.musicbot.console.commands;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.CommandExecutor;
import net.jandie1505.musicbot.utilities.BotStatus;

import java.util.List;

public class CommandsCommand implements CommandExecutor {
    private final MusicBot musicBot;

    public CommandsCommand(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public String onCommand(String command, String[] args) {

        if (this.musicBot.getBotStatus() != BotStatus.ACTIVE) {
            return "This command is disabled because the bot is not started";
        }

        if (args.length < 1) {
            return "Usage: commands delete/setup/get";
        }

        switch (args[0]) {
            case "delete" -> {
                this.musicBot.deleteCommands();
                return "Delete commands requested";
            }
            case "setup" -> {
                this.musicBot.upsertCommands();
                return "Setup commands requested";
            }
            case "get" -> {
                List<Command> commands = this.musicBot.getShardManager().retrieveApplicationInfo().getJDA().retrieveCommands().complete();

                String response = "COMMANDS:";

                for (Command cmd : commands) {
                    response = response + "\n" + cmd.getIdLong() + " " + cmd.getName();
                }

                return response;
            }
            default -> {
                return "Unknown subcommand";
            }
        }
    }
}
