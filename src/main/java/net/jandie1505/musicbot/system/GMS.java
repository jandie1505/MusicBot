package net.jandie1505.musicbot.system;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.database.GuildData;

public class GMS {
    private final MusicBot musicBot;
    
    public GMS(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    public void setupGuild(Guild g) {

        if (g == null) {
            return;
        }

        if (!this.musicBot.getConfigManager().getConfig().isPublicMode() && !this.musicBot.getDatabaseManager().isGuildWhitelisted(g.getIdLong())) {
            return;
        }

        if(g != null) {
            String guildId = g .getId();
            if(!this.musicBot.getConfigManager().getConfig().isPublicMode() && !this.musicBot.getDatabaseManager().isGuildWhitelisted(g.getIdLong())) {
                g.leave().queue();
                this.logDebug("Removed bot from guild " + guildId + " because it is not whitelisted");
            } else {
                if(!g.getSelfMember().hasPermission(Permission.CREATE_INSTANT_INVITE)
                        || !g.getSelfMember().hasPermission(Permission.NICKNAME_CHANGE)
                        || !g.getSelfMember().hasPermission(Permission.VIEW_CHANNEL)
                        || !g.getSelfMember().hasPermission(Permission.MESSAGE_SEND)
                        || !g.getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)
                        || !g.getSelfMember().hasPermission(Permission.MESSAGE_ATTACH_FILES)
                        || !g.getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)
                        || !g.getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)
                        || !g.getSelfMember().hasPermission(Permission.VOICE_CONNECT)
                        || !g.getSelfMember().hasPermission(Permission.VOICE_SPEAK)
                        || !g.getSelfMember().hasPermission(Permission.VOICE_USE_VAD)) {
                    g.leave().queue();
                    this.logDebug("Removed bot from guild " + guildId + " because of missing permissions");
                } else {
                    this.logDebug("Guild " + g.getId() + " was set up");
                }
            }
        }
    }

    public void setupCommands(Guild g) {
        /*
        if(g != null) {
            String guildId = g.getId();
            g.retrieveCommands().queue(commands -> {
                try {
                    for(Command cmd : commands) {
                        cmd.delete().queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                            leaveGuild(g.getId(), "Missing permissions for slash commands");
                            this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                        }));
                    }

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    CommandData nowplayingCommand = new CommandData("nowplaying", "Shows information about the song which is currently playing");
                    CommandData queueCommand = new CommandData("queue", "Shows the current queue");
                    CommandData playCommand = new CommandData("play", "Play a song")
                            .addOptions(new OptionData(OptionType.STRING, "song", "The song link / song name / playlist link you want to play"));
                    CommandData removeCommand = new CommandData("remove", "Remove a specific song from the queue")
                            .addOptions(new OptionData(OptionType.INTEGER, "index", "The index of the song you want to remove").setRequired(true));
                    CommandData searchCommand = new CommandData("search", "YTSearchHandler youtube")
                            .addOptions(new OptionData(OptionType.STRING, "query", "The text you want to search for"));
                    CommandData shuffleCommand = new CommandData("shuffle", "Shuffle the queue");
                    CommandData skipCommand = new CommandData("skip", "Skip a song")
                            .addOptions(new OptionData(OptionType.INTEGER, "position", "Skip to a specific queue position"));
                    CommandData removeUserCommand = new CommandData("removeuser", "Removes all songs by a specific member")
                            .addOptions(new OptionData(OptionType.USER, "member", "The member you want to remove the music from").setRequired(true));
                    CommandData forceskipCommand = new CommandData("forceskip", "Force skip a song")
                            .addOptions(new OptionData(OptionType.INTEGER, "position", "Skip to a specific queue position"));
                    CommandData movetrackCommand = new CommandData("movetrack", "Move a specific track in queue")
                            .addOptions(new OptionData(OptionType.INTEGER, "from", "The track you want to move").setRequired(true))
                            .addOptions(new OptionData(OptionType.INTEGER, "to", "The queue position you want to move the track to").setRequired(true));
                    CommandData playnowCommand = new CommandData("playnow", "Stop the current song and play the specified song immediately")
                            .addOptions(new OptionData(OptionType.STRING, "song", "The song you want to play").setRequired(true));
                    CommandData stopCommand = new CommandData("stop", "Stop playing music");
                    CommandData volumeCommand = new CommandData("volume", "Change the volume")
                            .addOptions(new OptionData(OptionType.INTEGER, "volume", "Change the volume to this value").setRequired(true));
                    CommandData leaveCommand = new CommandData("leave", "Leave the voice channel");

                    SubcommandData mbsettingsInfoCommand = new SubcommandData("info", "See an overview of all settings");
                    SubcommandData mbsettingsDJRoleCommand = new SubcommandData("djrole", "Add/remove/clear dj roles")
                            .addOptions(new OptionData(OptionType.STRING, "action", "add/remove").setRequired(true).addChoice("add", "add").addChoice("remove", "remove").addChoice("clear", "clear"))
                            .addOptions(new OptionData(OptionType.ROLE, "role", "Only required if you have chosen add/remove"));
                    CommandData mbsettingsCommand = new CommandData("mbsettings", "Music bot settings command for administrators")
                            .addSubcommands(mbsettingsInfoCommand)
                            .addSubcommands(mbsettingsDJRoleCommand);

                    g.upsertCommand(nowplayingCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(queueCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(playCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(removeCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(searchCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(shuffleCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(skipCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(removeUserCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(forceskipCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(movetrackCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(playnowCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(stopCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(volumeCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(leaveCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(mbsettingsCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        this.logDebug("Left guild " + guildId + " for missing slash command permissions");
                    }));

                    this.logDebug("Reloaded commands on guild " + g.getId());
                } catch(PermissionException ignored) {}
            }, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                leaveGuild(g.getId(), "Missing permissions for slash commands");
                this.logDebug("Left guild " + guildId + " for missing slash command permissions");
            }));
        }

         */
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

        if (memberHasUserPermissions(m)) {
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

    // BLACKLIST
    public boolean isBlacklisted(Guild g, String link) {
        return false;
    }
    public boolean isBlacklisted(Guild g, Member m, AudioTrack audioTrack) {
        return false;
    }

    private void logInfo(String message) {
        MusicBot.LOGGER.info(message);
    }

    private void logDebug(String message) {
        MusicBot.LOGGER.debug(message);
    }

    public MusicBot getMusicBot() {
        return this.musicBot;
    }
}
