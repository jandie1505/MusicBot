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
import net.dv8tion.jda.api.sharding.ShardManager;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.Console;
import org.json.JSONArray;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GMS {
    private static ShardManager shardManager;

    public static void init() {
        shardManager = MusicBot.getShardManager();
    }

    public static void reloadGuilds(boolean completeReload) {
        Console.messageGMS("Checking database guilds...");
        for(String guildId : DatabaseManager.getRegisteredGuilds()) {
            Guild g = shardManager.getGuildById(guildId);
            if(g == null) {
                if(!MusicBot.completeOnline()) {
                    Console.messageGMS("Guild " + guildId + " will not be deleted because not all shards are online", true);
                } else {
                    DatabaseManager.deleteGuild(guildId);
                    Console.messageGMS("Deleted guild " + guildId + " because it's null", true);
                }
            }
        }
        Console.messageGMS("Checking discord guilds...");
        for(Guild g : shardManager.getGuilds()) {
            if(g != null) {
                if(completeReload) {
                    setupGuild(g);
                } else {
                    if(!DatabaseManager.getRegisteredGuilds().contains(g.getId())) {
                        setupGuild(g);
                        Console.messageGMS("Added guild " + g.getId() + " to database", true);
                    }
                }
            }
        }
        Console.messageGMS("Reloaded guilds");
    }

    public static void leaveGuild(String guildId) {
        Guild g = shardManager.getGuildById(guildId);
        if(g != null) {
            g.leave().queue();
        }
        DatabaseManager.deleteGuild(guildId);
        Console.messageDB("Left guild " + guildId, true);
    }

    public static void leaveGuild(String guildId, String reason) {
        Guild g = shardManager.getGuildById(guildId);
        if(g != null) {
            g.retrieveOwner().queue(member -> {
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder botLeftGuildMessage = new EmbedBuilder()
                            .setTitle("Left your server")
                            .addField("Name (ID):", g.getName() + " (" + g.getId() + ")", false)
                            .addField("Reason:", reason, false)
                            .setColor(Color.RED);
                    privateChannel.sendMessage("Left your server " + g.getName() + " (" + g.getId() + ")").setEmbeds(botLeftGuildMessage.build()).queue();
                    g.leave().queue();
                    Console.messageDB("Left guild " + guildId + " for reason " + reason, true);
                }, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, e -> {
                    g.leave().queue();
                    Console.messageDB("Left guild " + guildId + " for reason " + reason, true);
                }));
            }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MEMBER, e -> {
                g.leave().queue();
                Console.messageDB("Left guild " + guildId, true);
            }));
        }
        DatabaseManager.deleteGuild(guildId);
    }

    public static void setupGuild(Guild g) {
        if(g != null) {
            String guildId = g .getId();
            if(!MusicBot.getPublicMode() && !DatabaseManager.isGuildWhitelisted(g.getId())) {
                GMS.leaveGuild(g.getId());
                Console.messageGMS("Removed bot from guild " + guildId + " because it is not whitelisted");
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
                    GMS.leaveGuild(g.getId(), "Missing permissions");
                    Console.messageGMS("Removed bot from guild " + guildId + " because of missing permissions");
                } else {
                    DatabaseManager.registerGuild(g.getId());
                    setupCommands(g);
                    reloadDJRoles(g.getId());
                    Console.messageGMS("Guild " + g.getId() + " was set up");
                }
            }
        }
    }

    public static void setupCommands(Guild g) {
        /*
        if(g != null) {
            String guildId = g.getId();
            g.retrieveCommands().queue(commands -> {
                try {
                    for(Command cmd : commands) {
                        cmd.delete().queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                            leaveGuild(g.getId(), "Missing permissions for slash commands");
                            Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
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
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(queueCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(playCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(removeCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(searchCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(shuffleCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(skipCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(removeUserCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(forceskipCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(movetrackCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(playnowCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(stopCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(volumeCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(leaveCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));
                    g.upsertCommand(mbsettingsCommand).queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                        leaveGuild(g.getId(), "Missing permissions for slash commands");
                        Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
                    }));

                    Console.messageGMS("Reloaded commands on guild " + g.getId());
                } catch(PermissionException ignored) {}
            }, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
                leaveGuild(g.getId(), "Missing permissions for slash commands");
                Console.messageGMS("Left guild " + guildId + " for missing slash command permissions");
            }));
        }

         */
    }

    public static Invite createGuildInvite(Guild g) {
        try {
            if(g != null) {
                return g.getTextChannels().get(0).createInvite().complete();
            } else {
                return null;
            }
        } catch(PermissionException | ErrorResponseException e) {
            return null;
        }
    }

    // DJ Roles
    public static void addDJRole(String guildId, String roleId) {
        try {
            JSONArray moderatorRoles = new JSONArray(DatabaseManager.getDJRoles(guildId));
            if(!moderatorRoles.toList().contains(roleId)) {
                moderatorRoles.put(roleId);
                DatabaseManager.setDJRoles(guildId, moderatorRoles.toString());
                Console.messageGMS("Added moderator role " + roleId + " on guild " + guildId);
            }
        } catch(Exception ignored) {}
    }

    public static void removeDJRole(String guildId, String roleId) {
        try {
            JSONArray moderatorRoles = new JSONArray(DatabaseManager.getDJRoles(guildId));
            if(moderatorRoles.toList().contains(roleId)) {
                moderatorRoles.remove(moderatorRoles.toList().indexOf(roleId));
                DatabaseManager.setDJRoles(guildId, moderatorRoles.toString());
                Console.messageGMS("Removed moderator role " + roleId + " on guild " + guildId);
            }
        } catch(Exception ignored) {}
    }

    public static void clearDJRoles(String guildId) {
        try {
            JSONArray moderatorRoles = new JSONArray(DatabaseManager.getDJRoles(guildId));
            moderatorRoles.clear();
            DatabaseManager.setDJRoles(guildId, moderatorRoles.toString());
            Console.messageGMS("Cleared moderator roles on guild " + guildId);
        } catch(Exception ignored) {}
    }

    public static List<String> getDJRoles(String guildId) {
        List<String> returnList = new ArrayList<>();

        try {
            JSONArray moderatorRoles = new JSONArray(DatabaseManager.getDJRoles(guildId));
            for(Object roleIdObject : moderatorRoles) {
                String roleId = (String) roleIdObject;
                returnList.add(roleId);
            }
        } catch(Exception ignored) {}

        return returnList;
    }

    public static void reloadDJRoles(String guildId) {
        try {
            Guild g = shardManager.getGuildById(guildId);
            if(g != null) {
                for(String roleId : getDJRoles(guildId)) {
                    Role role = g.getRoleById(roleId);
                    if(role == null) {
                        removeDJRole(guildId, roleId);
                    }
                }
                Console.messageGMS("Reloaded moderator roles on guild " + guildId);
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
    public static boolean memberHasUserPermissions(Member m) {
        return true;
        /*if(m != null) {
            if(memberHasDJPermissions(m)) {
                return true;
            } else if(DatabaseManager.getRestrictToRoles(m.getGuild().getId()) >= 1) {
                return true;
            }
        }
        return false;

         */
    }

    public static boolean memberHasDJPermissions(Member m) {
        if(memberHasAdminPermissions(m)) {
            return true;
        } else if(m.hasPermission(Permission.MANAGE_CHANNEL) || m.hasPermission(Permission.MANAGE_SERVER)) {
            return true;
        } else if(DatabaseManager.getRestrictToRoles(m.getGuild().getId()) >= 2) {
            return true;
        } else {
            for(String roleId : GMS.getDJRoles(m.getGuild().getId())) {
                Role role = shardManager.getRoleById(roleId);
                if(role != null) {
                    if(m.getRoles().contains(role)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean memberHasAdminPermissions(Member m) {
        if(m.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }
        return false;
    }

    // BLACKLIST
    public static boolean isBlacklisted(Guild g, String link) {
        if(g != null) {
            return !DatabaseManager.getGlobalBlacklist().contains(link) || !DatabaseManager.getBlacklist(g.getId()).contains(link);
        } else {
            return false;
        }
    }
    public static boolean isBlacklisted(Guild g, Member m, AudioTrack audioTrack) {
        if(g != null) {
            if(DatabaseManager.getGlobalBlacklist().contains(audioTrack.getInfo().uri) || DatabaseManager.getGlobalBlacklist().contains(audioTrack.getIdentifier())) {
                return true;
            }
            if(DatabaseManager.getGlobalArtistBlacklist().contains(audioTrack.getInfo().author)) {
                return true;
            }
            for(String string : DatabaseManager.getGlobalKeywordBlacklist()) {
                if(audioTrack.getInfo().title.contains(string)) {
                    return true;
                }
            }

            if(GMS.memberHasAdminPermissions(m)) {
                if(DatabaseManager.getBlacklist(g.getId()).contains(audioTrack.getInfo().uri) || DatabaseManager.getBlacklist(g.getId()).contains(audioTrack.getIdentifier())) {
                    return true;
                }
                if(DatabaseManager.getArtistBlacklist(g.getId()).contains(audioTrack.getInfo().author)) {
                    return true;
                }
                for(String string : DatabaseManager.getKeywordBlacklist(g.getId())) {
                    if(audioTrack.getInfo().title.contains(string)) {
                        return true;
                    }
                }
            }

            return false;
        } else {
            return false;
        }
    }
}
