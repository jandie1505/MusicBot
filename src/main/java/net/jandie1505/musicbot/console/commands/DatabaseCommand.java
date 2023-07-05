package net.jandie1505.musicbot.console.commands;

import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.CommandExecutor;
import net.jandie1505.musicbot.database.GuildData;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class DatabaseCommand implements CommandExecutor {
    private final MusicBot musicBot;

    public DatabaseCommand(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public String onCommand(String command, String[] args) {
        try {

            if (args.length == 0) {
                return "Database command help:\n" +
                        "database guild\n" +
                        "database whitelist\n" +
                        "database music-blacklist";
            }

            switch (args[0]) {
                case "guild" -> {

                    if (args.length < 2) {
                        return "Database command help:\n" +
                                "database guild list\n" +
                                "database guild add <guildId>\n" +
                                "database guild remove <guildId>\n" +
                                "database guild info <guildId>\n" +
                                "database guild update <guildId> djRoles/restrictToRoles/ephemeralState <value>";
                    }

                    switch (args[1]) {
                        case "list" -> {
                            String output = "Registered Guilds:";

                            for (GuildData guildData : this.musicBot.getDatabaseManager().getRegisteredGuilds()) {
                                output = output + "\n" + guildData.getGuildId();
                            }

                            output = output + "\nIf a guild is not listed here, it does not mean that the bot is not on this server";

                            return output;
                        }
                        case "add" -> {

                            if (args.length < 3) {
                                return "Usage: database guild add <guildId>";
                            }

                            this.musicBot.getDatabaseManager().updateGuild(this.musicBot.getDatabaseManager().getGuild(Long.parseLong(args[2])));
                            return "Added guild (if it wasn't already registered before)";
                        }
                        case "remove" -> {

                            if (args.length < 3) {
                                return "Usage: database guild remove <guildId>";
                            }

                            this.musicBot.getDatabaseManager().deleteGuild(Long.parseLong(args[2]));
                            return "Deleted guild (if it existed before)";
                        }
                        case "info" -> {

                            if (args.length < 3) {
                                return "Usage: database guild info <guildId>";
                            }

                            GuildData guildData = this.musicBot.getDatabaseManager().getGuild(Long.parseLong(args[2]));

                            return "Guild Information:\n" +
                                    "guildId: " + guildData.getGuildId() + "\n" +
                                    "djRoles: " + guildData.getDjRoles() + "\n" +
                                    "restrictToRoles: " + guildData.getRestrictToRoles() + "\n" +
                                    "ephemeralState: " + guildData.isEphemeralState();
                        }
                        case "update" -> {

                            if (args.length < 5) {
                                return "Usage: database guild update <guildId> djRoles/restrictToRoles/ephemeralState <value>";
                            }

                            GuildData guildData = this.musicBot.getDatabaseManager().getGuild(Long.parseLong(args[2]));

                            switch (args[3]) {
                                case "djRoles" -> {

                                    if (args[4].length() < 1) {
                                        return "You can specifythe following: +<roleId>, -<roleId> or s[<roleId>,<roleId>,...]";
                                    }

                                    switch (args[4].charAt(0)) {
                                        case '+' -> {
                                            List<Long> djRoles = guildData.getDjRoles();
                                            djRoles.add(Long.parseLong(args[4].substring(1)));
                                            this.musicBot.getDatabaseManager().updateGuild(guildData);
                                            return "Updated value djRoles (added)";
                                        }
                                        case '-' -> {
                                            List<Long> djRoles = guildData.getDjRoles();
                                            djRoles.remove(Long.parseLong(args[4].substring(1)));
                                            this.musicBot.getDatabaseManager().updateGuild(guildData);
                                            return "Updated value djRoles (removed)";
                                        }
                                        case 's' -> {
                                            guildData.setDJRolesFromJSONArray(args[4].substring(1));
                                            this.musicBot.getDatabaseManager().updateGuild(guildData);
                                            return "Updated value djRoles (set)";
                                        }
                                        default -> {
                                            return "Please specify an action (+<roleId>, -<roleId>, s[<roleId>,<roleId>,...])";
                                        }
                                    }

                                }
                                case "restrictToRoles" -> {
                                    guildData.setRestrictToRoles(Integer.parseInt(args[4]));
                                    this.musicBot.getDatabaseManager().updateGuild(guildData);
                                    return "Updated value restrictToRoles";
                                }
                                case "ephemeralState" -> {
                                    guildData.setEphemeralState(Boolean.parseBoolean(args[4]));
                                    this.musicBot.getDatabaseManager().updateGuild(guildData);
                                    return "Updated ephemeralState";
                                }
                                default -> {
                                    return "Invalid value";
                                }
                            }

                        }
                        default -> {
                            return "Unknown subcommand. Run command without arguments for help.";
                        }
                    }

                }
                default -> {
                    return "Unknown subcommand. Run command without arguments for help.";
                }
            }

        } catch (IllegalArgumentException e) {
            return "Illegal argument";
        }
    }
}
