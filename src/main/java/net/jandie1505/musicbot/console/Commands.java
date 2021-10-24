package net.jandie1505.musicbot.console;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.system.DatabaseManager;
import net.jandie1505.musicbot.system.GMS;
import net.jandie1505.musicbot.system.MusicManager;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static String command(String command) {
        String[] cmd = command.split(" ");
        String returnString = "";

        try {
            if(cmd.length >= 1) {
                if(cmd[0].equalsIgnoreCase("guild")) {
                    if(cmd.length >= 2) {
                        if(cmd[1].equalsIgnoreCase("list-ids")) {
                            List<String> guildIds = new ArrayList<>();
                            for(Guild g : MusicBot.getShardManager().getGuilds()) {
                                if(g != null) {
                                    guildIds.add(g.getId());
                                }
                            }
                            returnString = guildIds.toString();
                        } else if(cmd[1].equalsIgnoreCase("list")) {
                            returnString = "GUILD ID | GUILD NAME | GUILD OWNER\n";
                            for(Guild g : MusicBot.getShardManager().getGuilds()) {
                                if(g != null) {
                                    Member owner = g.retrieveOwner().complete();
                                    if(owner != null) {
                                        returnString = returnString + g.getId() + " | " + g.getName() + " | " + owner.getUser().getName() + "#" + owner.getUser().getDiscriminator() + " (" + owner.getId() + ")\n";
                                    } else {
                                        returnString = returnString + g.getId() + " | " + g.getName() + " | ---\n";
                                    }
                                }
                            }
                        } else if(cmd[1].equalsIgnoreCase("leave")) {
                            if(cmd.length == 3) {
                                GMS.leaveGuild(cmd[2]);
                                returnString = "SENT LEAVE GUILD COMMAND";
                            }
                        } else if(cmd[1].equalsIgnoreCase("reload")) {
                            if(cmd.length == 2) {
                                GMS.reloadGuilds(false);
                                returnString = "SENT RELOAD GUILDS COMMAND";
                            } else if(cmd.length == 3) {
                                Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                                if(g != null) {
                                    GMS.setupGuild(g);
                                    returnString = "SENT RELOAD GUILD COMMAND";
                                } else {
                                    returnString = "GUILD NOT AVAILABLE";
                                }
                            }
                        } else if(cmd[1].equalsIgnoreCase("complete-reload")) {
                            if(cmd.length == 2) {
                                GMS.reloadGuilds(true);
                                returnString = "SENT RELOAD GUILDS COMMAND";
                            }
                        } else if(cmd[1].equalsIgnoreCase("invite")) {
                            if(cmd.length == 3) {
                                Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                                if(g != null) {
                                    Invite invite = GMS.createGuildInvite(g);
                                    if(invite != null) {
                                        returnString = "INVITE LINK FOR GUILD " + g.getId() + ": " + invite.getUrl();
                                    } else {
                                        returnString = "INVITE IS NULL; NO PERMISSION OR EXPIRED INVITE";
                                    }
                                } else {
                                    returnString = "GUILD IS NULL";
                                }
                            }
                        } else if(cmd[1].equalsIgnoreCase("whitelist")) {
                            if(cmd.length == 3) {
                                if(cmd[2].equalsIgnoreCase("list")) {
                                    returnString = DatabaseManager.getGuildWhitelist().toString();
                                } else if(cmd[2].equalsIgnoreCase("clear")) {
                                    DatabaseManager.clearGuildWhitelist();
                                    returnString = "SENT CLEAR GUILD WHITELIST COMMAND";
                                }
                            } else if(cmd.length == 4) {
                                if(cmd[2].equalsIgnoreCase("add")) {
                                    DatabaseManager.addGuildToWhitelist(cmd[3]);
                                    returnString = "SENT ADD GUILD TO WHITELIST COMMAND";
                                } else if(cmd[2].equalsIgnoreCase("remove")) {
                                    DatabaseManager.removeGuildFromWhitelist(cmd[3]);
                                    returnString = "SENT REMOVE GUILD FROM WHITELIST COMMAND";
                                } else if(cmd[2].equalsIgnoreCase("isGuildWhitelisted")) {
                                    returnString = Boolean.toString(DatabaseManager.isGuildWhitelisted(cmd[3]));
                                }
                            }
                            else {
                                returnString = "guild whitelist add <guildId>\n" +
                                        "guild whitelist remove <guildId>\n" +
                                        "guild whitelist list\n" +
                                        "guild whitelist clear\n" +
                                        "guild whitelist isGuildWhitelisted <guildId>\n";
                            }
                        }
                    }
                } else if(cmd[0].equalsIgnoreCase("stop") || cmd[0].equalsIgnoreCase("shutdown")) {
                    returnString = "SENT SHUTDOWN COMMAND";
                    MusicBot.shutdown();
                } else if(cmd[0].equalsIgnoreCase("invite")) {
                    returnString = "https://discord.com/api/oauth2/authorize?client_id=" + MusicBot.getShardManager().retrieveApplicationInfo().getJDA().getSelfUser().getApplicationId() + "&permissions=2251418689&scope=bot%20applications.commands";
                } else if(cmd[0].equalsIgnoreCase("player")) {
                    if(cmd.length == 3) {
                        if(cmd[1].equalsIgnoreCase("disconnect")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                MusicManager.leaveVoiceChannel(g);
                                MusicManager.stop(g);
                                returnString = "SENT DISCONNECT COMMAND";
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("stop")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                MusicManager.stop(g);
                                returnString = "SENT STOP COMMAND";
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("queue")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                List<AudioTrack> audioTrackList = MusicManager.getQueue(g);
                                returnString = "* INDEX | TITLE | AUTHOR | DURATION | URL *\n";
                                int index = 0;
                                for(AudioTrack track : audioTrackList) {
                                    returnString = returnString + "* " + index + " | " + track.getInfo().title + " | " + track.getInfo().author + " | " + track.getDuration() + " | " + track.getInfo().uri + " *\n";
                                    index++;
                                }
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("next")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                MusicManager.next(g);
                                returnString = "SENT NEXT COMMAND";
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("clear")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                MusicManager.clear(g);
                                returnString = "SENT CLEAR COMMAND";
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("pause")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                if(MusicManager.isPaused(g)) {
                                    MusicManager.setPause(g, false);
                                    returnString = "SENT RESUME COMMAND";
                                } else {
                                    MusicManager.setPause(g, true);
                                    returnString = "SENT PAUSE COMMAND";
                                }
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("ispaused")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                returnString = "PAUSE STATE: " + MusicManager.isPaused(g);
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("nowplaying")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                AudioTrack track = MusicManager.getPlayingTrack(g);
                                if(track != null) {
                                    returnString = "NOW PLAYING:\n" +
                                            "Name: " + track.getInfo().title + "\n" +
                                            "Author: " + track.getInfo().author + "\n" +
                                            "URL: " + track.getInfo().uri + "\n" +
                                            "Duration: " + track.getPosition() + "/" + track.getDuration() + "\n" +
                                            "Paused: " + MusicManager.isPaused(g);
                                } else {
                                    returnString = "THE BOT IS NOTHING PLAYING";
                                }
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        }
                    } else if(cmd.length == 4) {
                        if(cmd[1].equalsIgnoreCase("connect")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                VoiceChannel voiceChannel = g.getVoiceChannelById(cmd[3]);
                                if(voiceChannel != null) {
                                    MusicManager.joinVoiceChannel(voiceChannel);
                                } else {
                                    returnString = "VOICE CHANNEL IS NULL";
                                }
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("add")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                MusicManager.add(g, cmd[3]);
                                returnString = "SENT ADD COMMAND";
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        } else if(cmd[1].equalsIgnoreCase("remove")) {
                            Guild g = MusicBot.getShardManager().getGuildById(cmd[2]);
                            if(g != null) {
                                MusicManager.remove(g, Integer.parseInt(cmd[3]));
                                returnString = "SENT PLAY COMMAND";
                            } else {
                                returnString = "GUILD IS NULL";
                            }
                        }
                    } else {
                        returnString = "player connect <guildId> <channelId>\n" +
                                "player disconnect <guildId>\n" +
                                "player add <guildId> <source>\n" +
                                "player stop <guildId>\n" +
                                "player next <guildId>\n" +
                                "player queue <guildId>\n" +
                                "player remove <guildId> <index>\n" +
                                "player clear <guildId>\n" +
                                "player pause <guildId>\n" +
                                "player ispaused <guildId>\n" +
                                "player nowplaying <guildId>\n";
                    }
                }
                else {
                    returnString = "UNKNOWN COMMAND";
                }
            }
        } catch(Exception e) {
            returnString = "COMMAND ERROR: " + e.getMessage();
        }
        return returnString;
    }
}
