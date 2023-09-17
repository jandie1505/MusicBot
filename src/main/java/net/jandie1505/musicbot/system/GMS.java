package net.jandie1505.musicbot.system;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.database.GuildData;

import java.util.ArrayList;
import java.util.List;

public class GMS {
    private final MusicBot musicBot;
    
    public GMS(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    public void setupGuild(Guild g) {

        if (g == null) {
            return;
        }

        if (!this.musicBot.getConfig().optBoolean("publicMode", false) && !this.musicBot.getDatabaseManager().isGuildWhitelisted(g.getIdLong())) {
            g.leave().queue();
            this.logInfo("Left guild " + g.getId() + " because it is not whitelisted");
            return;
        }

        if (!this.hasRequiredPermissions(g)) {
            g.leave().queue();
            this.logDebug("Left guild " + g.getId() + " because of missing permissions");
            return;
        }

        this.setupCommands(g);

        this.logDebug("Guild " + g.getId() + " was set up");
    }

    public boolean hasRequiredPermissions(Guild g) {

        if (g == null) {
            return false;
        }

        Member m = g.getSelfMember();

        return m.hasPermission(Permission.NICKNAME_CHANGE)
                || m.hasPermission(Permission.VIEW_CHANNEL)
                || m.hasPermission(Permission.MESSAGE_SEND)
                || m.hasPermission(Permission.MESSAGE_EMBED_LINKS)
                || m.hasPermission(Permission.MESSAGE_ATTACH_FILES)
                || m.hasPermission(Permission.MESSAGE_HISTORY)
                || m.hasPermission(Permission.MESSAGE_ADD_REACTION)
                || m.hasPermission(Permission.VOICE_CONNECT)
                || m.hasPermission(Permission.VOICE_SPEAK)
                || m.hasPermission(Permission.VOICE_USE_VAD);
    }

    public void deleteCommands(Guild g) {

        if (g == null) {
            return;
        }

        g.retrieveCommands().queue(commands -> {

            for (Command command : commands) {

                if (command.getApplicationIdLong() != g.getJDA().getSelfUser().getIdLong()) {
                    continue;
                }

                command.delete().queue(null, this.missingAccess(g));
            }

        });

    }

    public void setupCommands(Guild g) {

        if (g == null) {
            return;
        }

        g.retrieveCommands().queue(commands -> {

            List<String> registeredCommands = new ArrayList<>();

            for (Command cmd : commands) {

                if (cmd.getApplicationIdLong() != g.getJDA().getSelfUser().getIdLong()) {
                    continue;
                }

                registeredCommands.add(cmd.getName());
            }

            if (!registeredCommands.contains("nowplaying")) {
                g.upsertCommand(new CommandDataImpl("nowplaying", "Shows information about the song that is currently playing")).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("queue")) {
                g.upsertCommand(
                        new CommandDataImpl("queue", "Shows the current queue")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "index", "The index you want to start from")
                                )
                ).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("play")) {
                CommandData commandData = new CommandDataImpl("play", "Add a song to the queue")
                        .addOptions(
                                new OptionData(OptionType.STRING, "song", "The song link / search query / playlist link you want to play")
                                        .setRequired(true)
                        );

                g.upsertCommand(commandData).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("stop")) {
                g.upsertCommand(new CommandDataImpl("stop", "Pause the player / stop the player and clear queue")).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("disconnect")) {
                g.upsertCommand(new CommandDataImpl("disconnect", "Disconnect the bot from the voice channel")).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("forceskip")) {
                g.upsertCommand(new CommandDataImpl("forceskip", "Force-skip a song")).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("remove")) {
                g.upsertCommand(
                        new CommandDataImpl("remove", "Remove a song from the queue")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "index", "The index of the sond that should be removed")
                                                .setRequired(true)
                                )
                ).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("pause")) {
                g.upsertCommand(
                        new CommandDataImpl("pause", "Pause the player or set current pause state")
                                .addOptions(
                                        new OptionData(OptionType.BOOLEAN, "state", "The pause state (optional), run command without this option to just pause the player")
                                )
                ).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("resume")) {
                g.upsertCommand(new CommandDataImpl("resume", "Resume the player")).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("clear")) {
                g.upsertCommand(new CommandDataImpl("clear", "Clear the queue")).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("playnow")) {
                g.upsertCommand(
                        new CommandDataImpl("playnow", "Play a current track now instead of adding it to the queue")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "song", "The song that should be played").setRequired(true)
                                )
                ).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("movetrack")) {
                g.upsertCommand(
                        new CommandDataImpl("movetrack", "Move a track from a queue position to another")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "from", "The index of the track you want to move").setRequired(true),
                                        new OptionData(OptionType.INTEGER, "to", "The new index of the track you want to set").setRequired(true)
                                )
                ).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("shuffle")) {
                g.upsertCommand(new CommandDataImpl("shuffle", "Randomizes the queue order")).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("search")) {
                g.upsertCommand(
                        new CommandDataImpl("search", "Search for music")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "query", "What you want to search").setRequired(true)
                                )
                ).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("volume")) {
                g.upsertCommand(
                        new CommandDataImpl("volume", "Set the volume")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "volume", "The volume between 0 and 200").setRequired(true)
                                )
                ).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("connect")) {
                g.upsertCommand(
                        new CommandDataImpl("connect", "Connect the bot to a channel")
                                .addOptions(
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel the bot should be connected to")
                                )
                ).queue(null, this.missingAccess(g));
            }

            if (!registeredCommands.contains("mbsettings")) {
                g.upsertCommand(
                        new CommandDataImpl("mbsettings", "Manage music bot settings")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "action", "What do you want to do")
                                                .setRequired(true)
                                                .addChoices(
                                                        new Command.Choice("Show all Settings", 0),
                                                        new Command.Choice("Show DJ Roles", 1),
                                                        new Command.Choice("Add DJ Role", 2),
                                                        new Command.Choice("Remove DJ Role", 3),
                                                        new Command.Choice("Clear DJ Roles", 4),
                                                        new Command.Choice("Manage Restrict to Roles", 5),
                                                        new Command.Choice("Manage Ephemeral State", 6),
                                                        new Command.Choice("Manage Default Volume" , 7)
                                                ),
                                        new OptionData(OptionType.STRING, "value", "The value you want to set")
                                )

                ).queue(null, this.missingAccess(g));
            }

            this.logDebug("Commands on guild " + g.getIdLong() + " were set up");

        }, this.missingAccess(g));
    }

    private ErrorHandler missingAccess(Guild guild) {
        return new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
            guild.leave().queue();
            this.logDebug("Left guild " + guild.getId() + " because of missing access to slash commands");
        });
    }

    public void setupGuilds() {

        for (Guild g : this.musicBot.getShardManager().getGuilds()) {
            this.setupGuild(g);
        }

    }

    // PERMISSIONS
    /*
    RestrictToRoles States:
    0 = ONLY DJs and admins can use the bot
    1 = Normal users have the permission to add tracks to the queue and voteskip
    2 = Normal users have DJ permissions
     */
    public boolean memberHasUserPermissions(Member m) {

        if (memberHasAdminPermissions(m)) {
            return true;
        }

        if (memberHasDJPermissions(m)) {
            return true;
        }

        GuildData guildData = this.musicBot.getDatabaseManager().getGuild(m.getGuild().getIdLong());

        if (guildData.getRestrictToRoles() >= 1) {
            return true;
        }

        for (long roleId : guildData.getDjRoles()) {
            Role role = m.getGuild().getRoleById(roleId);

            if (role == null) {
                continue;
            }

            if (m.getRoles().contains(role)) {
                return true;
            }

        }

        return false;
    }

    public boolean memberHasDJPermissions(Member m) {

        if (memberHasAdminPermissions(m)) {
            return true;
        }

        if (m.hasPermission(Permission.MANAGE_CHANNEL) || m.hasPermission(Permission.MANAGE_SERVER)) {
            return true;
        }

        GuildData guildData = this.musicBot.getDatabaseManager().getGuild(m.getGuild().getIdLong());

        if (guildData.getRestrictToRoles() >= 2) {
            return true;
        }

        for (long roleId : guildData.getDjRoles()) {
            Role role = m.getGuild().getRoleById(roleId);

            if (role == null) {
                continue;
            }

            if (m.getRoles().contains(role)) {
                return true;
            }

        }

        return false;
    }

    public boolean memberHasAdminPermissions(Member m) {
        return m.hasPermission(Permission.ADMINISTRATOR);
    }

    // LOGGER

    private void logInfo(String message) {
        MusicBot.LOGGER.info(message);
    }

    private void logDebug(String message) {
        MusicBot.LOGGER.debug(message);
    }

    // BOT

    public MusicBot getMusicBot() {
        return this.musicBot;
    }
}
