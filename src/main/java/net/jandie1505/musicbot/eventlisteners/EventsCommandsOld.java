package net.jandie1505.musicbot.eventlisteners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.database.GuildData;
import net.jandie1505.musicbot.utilities.Messages;
import net.jandie1505.musicbot.utilities.SpotifySearchHandler;
import net.jandie1505.musicbot.utilities.YTSearchHandler;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EventsCommandsOld extends ListenerAdapter {
    
    private MusicBot musicBot;
    
    public EventsCommandsOld(MusicBot musicBot) {
        this.musicBot = musicBot;
    }
    
    // EVENT
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getMember() != null && event.getGuild() != null) {
            if(event.getName().equalsIgnoreCase("skip")) {
                this.skipCommand(event);
            }
        }
    }

    // COMMANDS

    private void skipCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasUserPermissions(event.getMember())) {
            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(this.musicBot.getMusicManager().getSkipvotes(event.getGuild()).contains(event.getMember())) {
                    this.musicBot.getMusicManager().removeSkipvote(event.getGuild(), event.getMember());
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":negative_squared_cross_mark: Removed skipvote (" + (this.musicBot.getMusicManager().getVoteCount(event.getGuild())-1) + "/" + ((event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size()-1)/2) + ")")
                            .setColor(Color.YELLOW);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                } else {
                    this.musicBot.getMusicManager().addSkipvote(event.getGuild(), event.getMember());
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":fast_forward:  You have voted to skip (" + (this.musicBot.getMusicManager().getVoteCount(event.getGuild())+1) + "/" + ((event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size()-1)/2) + ")")
                            .setColor(Color.YELLOW);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                }
            } else {
                event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
            }
        }
    }

    /*
    private void mbsettingsCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasAdminPermissions(event.getMember())) {
            if(event.getSubcommandName() != null) {
                if(event.getSubcommandName().equalsIgnoreCase("info")) {
                    event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                    String djroles = "";
                    for(String DJRoleId : this.musicBot.getGMS().getDJRoles(event.getGuild().getId())) {
                        Role role = event.getGuild().getRoleById(DJRoleId);
                        if(role != null) {
                            djroles = djroles + role.getName() + " (" + role.getId() + ")\n";
                        }
                    }
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle("MusicBot Settings")
                            .setDescription("MusicBot by jandie1505")
                            .addField("DJ Roles:", djroles, false)
                            .addField("Ephemeral replies:", String.valueOf(this.getEphemeralState(event.getGuild().getIdLong())), false)
                            .addField("Blacklisted tracks:", String.valueOf(this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId()).size()) + " blacklisted tracks\n" + String.valueOf(this.musicBot.getDatabaseManager().getGlobalBlacklist().size()) + " global blacklisted tracks", false);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                } else if(event.getSubcommandName().equalsIgnoreCase("djrole")) {
                    if(event.getOption("action") != null) {
                        if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            this.musicBot.getGMS().reloadDJRoles(event.getGuild().getId());
                            if(event.getOption("role") != null) {
                                this.musicBot.getGMS().addDJRole(event.getGuild().getId(), event.getOption("role").getAsRole().getId());
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":white_check_mark:  Added DJ Role")
                                        .setColor(Color.GREEN);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Role required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("remove")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            this.musicBot.getGMS().reloadDJRoles(event.getGuild().getId());
                            if(event.getOption("role") != null) {
                                this.musicBot.getGMS().removeDJRole(event.getGuild().getId(), event.getOption("role").getAsRole().getId());
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":white_check_mark:  Removed DJ Role")
                                        .setColor(Color.GREEN);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Role required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("clear")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            this.musicBot.getGMS().clearDJRoles(event.getGuild().getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Cleared DJ Roles")
                                    .setColor(Color.GREEN);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    }
                } else if(event.getSubcommandName().equalsIgnoreCase("ephemeral")) {
                    if(event.getOption("state") != null) {
                        event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                        this.musicBot.getDatabaseManager().setEphemeralState(event.getGuild().getId(), event.getOption("state").getAsBoolean());
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":white_check_mark:  Set ephemeral state to " + event.getOption("state").getAsBoolean())
                                .setColor(Color.GREEN);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                } else if(event.getSubcommandName().equalsIgnoreCase("blacklist")) {
                    if(event.getOption("action") != null) {
                        if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            if(event.getOption("link") != null) {
                                if(!this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId()).contains(event.getOption("link").getAsString())) {
                                    this.musicBot.getDatabaseManager().addToBlacklist(event.getGuild().getId(), event.getOption("link").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Added link to blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already added to blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Link required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("remove")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            if(event.getOption("link") != null) {
                                if(this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId()).contains(event.getOption("link").getAsString())) {
                                    this.musicBot.getDatabaseManager().deleteFromBlacklist(event.getGuild().getId(), event.getOption("link").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Removed link from blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already not in blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Link required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("clear")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            this.musicBot.getDatabaseManager().clearBlacklist(event.getGuild().getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Cleared blacklist")
                                    .setColor(Color.GREEN);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("list")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            String blacklist = "";
                            int index = 0;
                            for(String string : this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId())) {
                                String current = string + "\n";
                                String nextString = blacklist + current;
                                if(nextString.length() <= 950) {
                                    blacklist = blacklist + current;
                                } else {
                                    blacklist = blacklist + "`+ " + (this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId()).size()-index) + " entries.`";
                                    break;
                                }
                                index++;
                            }
                            EmbedBuilder queueFull = new EmbedBuilder()
                                    .addField("Blacklist:", blacklist, false);
                            event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                        }
                    }
                } else if(event.getSubcommandName().equalsIgnoreCase("keywordblacklist")) {
                    if(event.getOption("action") != null) {
                        if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            if(event.getOption("keyword") != null) {
                                if(!this.musicBot.getDatabaseManager().getKeywordBlacklist(event.getGuild().getId()).contains(event.getOption("keyword").getAsString())) {
                                    this.musicBot.getDatabaseManager().addToBlacklist(event.getGuild().getId(), event.getOption("keyword").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Added keyword to blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already added to keyword blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Keyword required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("remove")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            if(event.getOption("keyword") != null) {
                                if(this.musicBot.getDatabaseManager().getKeywordBlacklist(event.getGuild().getId()).contains(event.getOption("keyword").getAsString())) {
                                    this.musicBot.getDatabaseManager().deleteFromBlacklist(event.getGuild().getId(), event.getOption("keyword").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Removed keyword from blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already not in keyword blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Keyword required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("clear")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            this.musicBot.getDatabaseManager().clearBlacklist(event.getGuild().getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Cleared keyword blacklist")
                                    .setColor(Color.GREEN);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("list")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            String blacklist = "";
                            int index = 0;
                            for(String string : this.musicBot.getDatabaseManager().getKeywordBlacklist(event.getGuild().getId())) {
                                String current = string + "\n";
                                String nextString = blacklist + current;
                                if(nextString.length() <= 950) {
                                    blacklist = blacklist + current;
                                } else {
                                    blacklist = blacklist + "`+ " + (this.musicBot.getDatabaseManager().getKeywordBlacklist(event.getGuild().getId()).size()-index) + " entries.`";
                                    break;
                                }
                                index++;
                            }
                            EmbedBuilder queueFull = new EmbedBuilder()
                                    .addField("Keyword Blacklist:", blacklist, false);
                            event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                        }
                    }
                } else if(event.getSubcommandName().equalsIgnoreCase("artistblacklist")) {
                    if(event.getOption("action") != null) {
                        if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            if(event.getOption("artist") != null) {
                                if(!this.musicBot.getDatabaseManager().getArtistBlacklist(event.getGuild().getId()).contains(event.getOption("artist").getAsString())) {
                                    this.musicBot.getDatabaseManager().addToBlacklist(event.getGuild().getId(), event.getOption("artist").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Added artist to blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already added to artist blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Artist required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("remove")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            if(event.getOption("artist") != null) {
                                if(this.musicBot.getDatabaseManager().getArtistBlacklist(event.getGuild().getId()).contains(event.getOption("artist").getAsString())) {
                                    this.musicBot.getDatabaseManager().deleteFromBlacklist(event.getGuild().getId(), event.getOption("artist").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Removed artist from blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already not in artist blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Artist required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("clear")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            this.musicBot.getDatabaseManager().clearBlacklist(event.getGuild().getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Cleared artist blacklist")
                                    .setColor(Color.GREEN);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("list")) {
                            event.deferReply(this.getEphemeralState(event.getGuild().getIdLong())).queue();
                            String blacklist = "";
                            int index = 0;
                            for(String string : this.musicBot.getDatabaseManager().getArtistBlacklist(event.getGuild().getId())) {
                                String current = string + "\n";
                                String nextString = blacklist + current;
                                if(nextString.length() <= 950) {
                                    blacklist = blacklist + current;
                                } else {
                                    blacklist = blacklist + "`+ " + (this.musicBot.getDatabaseManager().getArtistBlacklist(event.getGuild().getId()).size()-index) + " entries.`";
                                    break;
                                }
                                index++;
                            }
                            EmbedBuilder queueFull = new EmbedBuilder()
                                    .addField("Artist Blacklist:", blacklist, false);
                            event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                        }
                    }
                }
            }
        }
    }

     */

    // UTILITY

    private EmbedBuilder getNotConnectedErrorMessage(){
        return new EmbedBuilder()
                .setDescription(":warning:  Not connected")
                .setColor(Color.RED);
    }

    public boolean getEphemeralState(long guildId) {
        GuildData guildData = this.musicBot.getDatabaseManager().getGuild(guildId);
        return guildData.isEphemeralState();
    }
}
