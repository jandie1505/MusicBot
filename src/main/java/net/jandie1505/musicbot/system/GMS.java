package net.jandie1505.musicbot.system;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.tasks.TaskGMSReload;
import net.jandie1505.musicbot.tasks.TaskGMSReloadComplete;
import org.json.JSONArray;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GMS {
    private final MusicBot musicBot;
    private final TaskGMSReload taskGMSReload;
    private final TaskGMSReloadComplete taskGMSReloadComplete;
    
    public GMS(MusicBot musicBot) {
        this.musicBot = musicBot;
        this.taskGMSReload = new TaskGMSReload(this);
        this.taskGMSReloadComplete = new TaskGMSReloadComplete(this);

        this.taskGMSReload.start();
        this.taskGMSReloadComplete.start();
    }

    public void setupGuild(Guild g) {

        if (g == null) {
            return;
        }

        if (!this.musicBot.getConfigManager().getConfig().isPublicMode() && !this.musicBot.getDatabaseManager().isGuildWhitelisted(g.getId())) {
            return;
        }

        if(g != null) {
            String guildId = g .getId();
            if(!this.musicBot.getConfigManager().getConfig().isPublicMode() && !this.musicBot.getDatabaseManager().isGuildWhitelisted(g.getId())) {
                this.leaveGuild(g.getId());
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
                    this.leaveGuild(g.getId());
                    this.logDebug("Removed bot from guild " + guildId + " because of missing permissions");
                } else {
                    this.musicBot.getDatabaseManager().registerGuild(g.getId());
                    setupCommands(g);
                    reloadDJRoles(g.getId());
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

    // DJ Roles
    public void addDJRole(String guildId, String roleId) {
        try {
            JSONArray moderatorRoles = new JSONArray(this.musicBot.getDatabaseManager().getDJRoles(guildId));
            if(!moderatorRoles.toList().contains(roleId)) {
                moderatorRoles.put(roleId);
                this.musicBot.getDatabaseManager().setDJRoles(guildId, moderatorRoles.toString());
                this.logDebug("Added moderator role " + roleId + " on guild " + guildId);
            }
        } catch(Exception ignored) {}
    }

    public void removeDJRole(String guildId, String roleId) {
        try {
            JSONArray moderatorRoles = new JSONArray(this.musicBot.getDatabaseManager().getDJRoles(guildId));
            if(moderatorRoles.toList().contains(roleId)) {
                moderatorRoles.remove(moderatorRoles.toList().indexOf(roleId));
                this.musicBot.getDatabaseManager().setDJRoles(guildId, moderatorRoles.toString());
                this.logDebug("Removed moderator role " + roleId + " on guild " + guildId);
            }
        } catch(Exception ignored) {}
    }

    public void clearDJRoles(String guildId) {
        try {
            JSONArray moderatorRoles = new JSONArray(this.musicBot.getDatabaseManager().getDJRoles(guildId));
            moderatorRoles.clear();
            this.musicBot.getDatabaseManager().setDJRoles(guildId, moderatorRoles.toString());
            this.logDebug("Cleared moderator roles on guild " + guildId);
        } catch(Exception ignored) {}
    }

    public List<String> getDJRoles(String guildId) {
        List<String> returnList = new ArrayList<>();

        try {
            JSONArray moderatorRoles = new JSONArray(this.musicBot.getDatabaseManager().getDJRoles(guildId));
            for(Object roleIdObject : moderatorRoles) {
                String roleId = (String) roleIdObject;
                returnList.add(roleId);
            }
        } catch(Exception ignored) {}

        return returnList;
    }

    public void reloadDJRoles(String guildId) {
        try {
            Guild g = this.musicBot.getShardManager().getGuildById(guildId);
            if(g != null) {
                for(String roleId : getDJRoles(guildId)) {
                    Role role = g.getRoleById(roleId);
                    if(role == null) {
                        removeDJRole(guildId, roleId);
                    }
                }
                this.logDebug("Reloaded moderator roles on guild " + guildId);
            }
        } catch(Exception ignored) {}
    }

    // PERMISSIONS
    /*
    RestrictToRoles States:
    0 = ONLY DJs and admins can use the bot
    1 = Normal users have the permission to add tracks to the queue and voteskip
    2 = Normal users have DJ permissions
     */
    public boolean memberHasUserPermissions(Member m) {
        return true;
        /*if(m != null) {
            if(memberHasDJPermissions(m)) {
                return true;
            } else if(this.musicBot.getDatabaseManager().getRestrictToRoles(m.getGuild().getId()) >= 1) {
                return true;
            }
        }
        return false;

         */
    }

    public boolean memberHasDJPermissions(Member m) {
        if(memberHasAdminPermissions(m)) {
            return true;
        } else if(m.hasPermission(Permission.MANAGE_CHANNEL) || m.hasPermission(Permission.MANAGE_SERVER)) {
            return true;
        } else if(this.musicBot.getDatabaseManager().getRestrictToRoles(m.getGuild().getId()) >= 2) {
            return true;
        } else {
            for(String roleId : this.getDJRoles(m.getGuild().getId())) {
                Role role = this.musicBot.getShardManager().getRoleById(roleId);
                if(role != null) {
                    if(m.getRoles().contains(role)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean memberHasAdminPermissions(Member m) {
        if(m.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }
        return false;
    }

    // BLACKLIST
    public boolean isBlacklisted(Guild g, String link) {
        if(g != null) {
            return !this.musicBot.getDatabaseManager().getGlobalBlacklist().contains(link) || !this.musicBot.getDatabaseManager().getBlacklist(g.getId()).contains(link);
        } else {
            return false;
        }
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
