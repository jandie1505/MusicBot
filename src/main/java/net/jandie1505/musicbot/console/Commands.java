package net.jandie1505.musicbot.console;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.jandie1505.musicbot.MusicBot;

import java.util.ArrayList;
import java.util.List;

public class Commands {

    public static String command(MusicBot musicBot, String command) {
        String[] cmd = command.split(" ");
        String returnString = "";

        try {
            if(cmd.length >= 1) {
                if(musicBot.completeOnline()) {
                    if(cmd[0].equalsIgnoreCase("guild")) {
                        returnString = guildCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("stop") || cmd[0].equalsIgnoreCase("shutdown")) {
                        returnString = "SENT SHUTDOWN COMMAND";
                        musicBot.shutdown();
                    } else if(cmd[0].equalsIgnoreCase("invite")) {
                        returnString = "https://discord.com/api/oauth2/authorize?client_id=" + musicBot.getShardManager().retrieveApplicationInfo().getJDA().getSelfUser().getApplicationId() + "&permissions=2251418689&scope=bot%20applications.commands";
                    } else if(cmd[0].equalsIgnoreCase("player")) {
                        returnString = playerCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("blacklist")) {
                        returnString = blacklistCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("keywordblacklist")) {
                        returnString = keywordBlacklistCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("artistblacklist")) {
                        returnString = artistBlacklistCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("cmdreload")) {
                        if(cmd.length == 2) {
                            if(cmd[1].equalsIgnoreCase("true")) {
                                musicBot.upsertCommands(true);
                                returnString = "SENT COMPLETE COMMANDS RELOAD COMMAND";
                            } else {
                                musicBot.upsertCommands(false);
                                returnString = "SENT COMMANDS RELOAD COMMAND";
                            }
                        } else {
                            musicBot.upsertCommands(false);
                            returnString = "SENT COMMANDS RELOAD COMMAND";
                        }
                    } else if(cmd[0].equalsIgnoreCase("shard") || cmd[0].equalsIgnoreCase("shards")) {
                        returnString = shardsCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("verbose")) {
                        returnString = verboseCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("help")) {
                        returnString = helpCommand(cmd);
                    }
                    else {
                        returnString = "Unknown command. Use help for a list of available commands.";
                    }
                } else {
                    if(cmd[0].equalsIgnoreCase("stop") || cmd[0].equalsIgnoreCase("shutdown")) {
                        returnString = "SENT SHUTDOWN COMMAND";
                        musicBot.shutdown();
                    } else if(cmd[0].equalsIgnoreCase("shard") || cmd[0].equalsIgnoreCase("shards")) {
                         returnString = shardsCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("verbose")) {
                        returnString = verboseCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("blacklist")) {
                        returnString = blacklistCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("keywordblacklist")) {
                        returnString = keywordBlacklistCommand(musicBot, cmd);
                    } else if(cmd[0].equalsIgnoreCase("artistblacklist")) {
                        returnString = artistBlacklistCommand(musicBot, cmd);
                    } else {
                        returnString = "The bot is in limited mode. Some commands are disabled.";
                    }
                }
            }
        } catch(Exception e) {
            returnString = "COMMAND ERROR: " + e.getMessage();
        }
        return returnString;
    }


    // COMMANDS
    private static String guildCommand(MusicBot musicBot, String[] cmd) {
        String returnString = "";
        if(cmd.length >= 2) {
            if(cmd[1].equalsIgnoreCase("list-ids")) {
                List<String> guildIds = new ArrayList<>();
                for(Guild g : musicBot.getShardManager().getGuilds()) {
                    if(g != null) {
                        guildIds.add(g.getId());
                    }
                }
                returnString = guildIds.toString();
            } else if(cmd[1].equalsIgnoreCase("list")) {
                returnString = "GUILD ID | GUILD NAME | GUILD OWNER\n";
                for(Guild g : musicBot.getShardManager().getGuilds()) {
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
                    musicBot.getGMS().leaveGuild(cmd[2]);
                    returnString = "SENT LEAVE GUILD COMMAND";
                }
            } else if(cmd[1].equalsIgnoreCase("reload")) {
                if(cmd.length == 2) {
                    musicBot.getGMS().reloadGuilds(false);
                    returnString = "SENT RELOAD GUILDS COMMAND";
                } else if(cmd.length == 3) {
                    Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                    if(g != null) {
                        musicBot.getGMS().setupGuild(g);
                        returnString = "SENT RELOAD GUILD COMMAND";
                    } else {
                        returnString = "GUILD NOT AVAILABLE";
                    }
                }
            } else if(cmd[1].equalsIgnoreCase("complete-reload")) {
                if(cmd.length == 2) {
                    musicBot.getGMS().reloadGuilds(true);
                    returnString = "SENT RELOAD GUILDS COMMAND";
                }
            }/* else if(cmd[1].equalsIgnoreCase("invite")) {
                if(cmd.length == 3) {
                    Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                    if(g != null) {
                        Invite invite = musicBot.getGMS().createGuildInvite(g);
                        if(invite != null) {
                            returnString = "INVITE LINK FOR GUILD " + g.getId() + ": " + invite.getUrl();
                        } else {
                            returnString = "INVITE IS NULL; NO PERMISSION OR EXPIRED INVITE";
                        }
                    } else {
                        returnString = "GUILD IS NULL";
                    }
                }
            }*/ else if(cmd[1].equalsIgnoreCase("whitelist")) {
                if(cmd.length == 3) {
                    if(cmd[2].equalsIgnoreCase("list")) {
                        returnString = musicBot.getDatabaseManager().getGuildWhitelist().toString();
                    } else if(cmd[2].equalsIgnoreCase("clear")) {
                        musicBot.getDatabaseManager().clearGuildWhitelist();
                        returnString = "SENT CLEAR GUILD WHITELIST COMMAND";
                    }
                } else if(cmd.length == 4) {
                    if(cmd[2].equalsIgnoreCase("add")) {
                        musicBot.getDatabaseManager().addGuildToWhitelist(cmd[3]);
                        returnString = "SENT ADD GUILD TO WHITELIST COMMAND";
                    } else if(cmd[2].equalsIgnoreCase("remove")) {
                        musicBot.getDatabaseManager().removeGuildFromWhitelist(cmd[3]);
                        returnString = "SENT REMOVE GUILD FROM WHITELIST COMMAND";
                    } else if(cmd[2].equalsIgnoreCase("isGuildWhitelisted")) {
                        returnString = Boolean.toString(musicBot.getDatabaseManager().isGuildWhitelisted(cmd[3]));
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
        return returnString;
    }

    private static String playerCommand(MusicBot musicBot, String[] cmd) {
        String returnString = "";
        if(cmd.length == 3) {
            if(cmd[1].equalsIgnoreCase("disconnect")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    musicBot.getMusicManager().disconnect(g);
                    musicBot.getMusicManager().stop(g);
                    returnString = "SENT DISCONNECT COMMAND";
                } else {
                    returnString = "GUILD IS NULL";
                }
            } else if(cmd[1].equalsIgnoreCase("stop")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    musicBot.getMusicManager().stop(g);
                    returnString = "SENT STOP COMMAND";
                } else {
                    returnString = "GUILD IS NULL";
                }
            } else if(cmd[1].equalsIgnoreCase("queue")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    List<AudioTrack> audioTrackList = musicBot.getMusicManager().getQueue(g);
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
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    musicBot.getMusicManager().next(g);
                    returnString = "SENT NEXT COMMAND";
                } else {
                    returnString = "GUILD IS NULL";
                }
            } else if(cmd[1].equalsIgnoreCase("clear")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    musicBot.getMusicManager().clear(g);
                    returnString = "SENT CLEAR COMMAND";
                } else {
                    returnString = "GUILD IS NULL";
                }
            } else if(cmd[1].equalsIgnoreCase("pause")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    if(musicBot.getMusicManager().isPaused(g)) {
                        musicBot.getMusicManager().setPause(g, false);
                        returnString = "SENT RESUME COMMAND";
                    } else {
                        musicBot.getMusicManager().setPause(g, true);
                        returnString = "SENT PAUSE COMMAND";
                    }
                } else {
                    returnString = "GUILD IS NULL";
                }
            } else if(cmd[1].equalsIgnoreCase("ispaused")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    returnString = "PAUSE STATE: " + musicBot.getMusicManager().isPaused(g);
                } else {
                    returnString = "GUILD IS NULL";
                }
            } else if(cmd[1].equalsIgnoreCase("nowplaying")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    AudioTrack track = musicBot.getMusicManager().getPlayingTrack(g);
                    if(track != null) {
                        returnString = "NOW PLAYING:\n" +
                                "Name: " + track.getInfo().title + "\n" +
                                "Author: " + track.getInfo().author + "\n" +
                                "URL: " + track.getInfo().uri + "\n" +
                                "Duration: " + track.getPosition() + "/" + track.getDuration() + "\n" +
                                "Paused: " + musicBot.getMusicManager().isPaused(g);
                    } else {
                        returnString = "THE BOT IS NOTHING PLAYING";
                    }
                } else {
                    returnString = "GUILD IS NULL";
                }
            }
        } else if(cmd.length == 4) {
            if(cmd[1].equalsIgnoreCase("connect")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    VoiceChannel voiceChannel = g.getVoiceChannelById(cmd[3]);
                    if(voiceChannel != null) {
                        musicBot.getMusicManager().connect(voiceChannel);
                    } else {
                        returnString = "VOICE CHANNEL IS NULL";
                    }
                } else {
                    returnString = "GUILD IS NULL";
                }
            } else if(cmd[1].equalsIgnoreCase("add")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    musicBot.getMusicManager().add(g, cmd[3], false);
                    returnString = "SENT ADD COMMAND";
                } else {
                    returnString = "GUILD IS NULL";
                }
            } else if(cmd[1].equalsIgnoreCase("remove")) {
                Guild g = musicBot.getShardManager().getGuildById(cmd[2]);
                if(g != null) {
                    musicBot.getMusicManager().remove(g, Integer.parseInt(cmd[3]));
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
        return returnString;
    }

    private static String shardsCommand(MusicBot musicBot, String[] cmd) {
        String returnString = "";
        if(cmd.length == 2) {
            if(cmd[1].equalsIgnoreCase("list")) {
                returnString = "* SHARD ID | STATUS | GUILDS *\n";
                for(JDA jda : musicBot.getShardManager().getShards()) {
                    returnString = returnString + "* " + jda.getShardInfo().getShardId() + " | " + jda.getStatus() + " | " + jda.getGuilds().size() + " *\n";
                }
                returnString = returnString + "* SHARDS ONLINE: " + musicBot.getShardManager().getShardsRunning() + "\n" +
                        "* SHARDS QUEUED: " + musicBot.getShardManager().getShardsQueued() + "\n" +
                        "* SHARDS COUNT: " + musicBot.getShardManager().getShards().size() + "\n" +
                        "* SHARDS TOTAL: " + musicBot.getShardManager().getShardsTotal() + "\n";
            } else if(cmd[1].equalsIgnoreCase("listraw")) {
                returnString = musicBot.getShardManager().getShards().toString();
            } else if(cmd[1].equalsIgnoreCase("restartall")) {
                musicBot.restartShards();
            } else if(cmd[1].equalsIgnoreCase("startall")) {
                musicBot.startShards();
            } else if(cmd[1].equalsIgnoreCase("stopall")) {
                musicBot.stopShards();
            } else if(cmd[1].equalsIgnoreCase("reload")) {
                musicBot.reloadShards();
                returnString = "SENT RELOAD SHARDS COMMAND";
            }
        } else if(cmd.length == 3) {
            if(cmd[1].equalsIgnoreCase("info")) {
                JDA jda = musicBot.getShardManager().getShardById(Integer.parseInt(cmd[2]));
                returnString = "SHARD INFO: " + jda.getShardInfo().getShardId() + "\n" +
                        "SHARD STRING: " + jda.getShardInfo().getShardString() + "\n" +
                        "GUILD COUNT: " + jda.getGuilds().size() + "\n" +
                        "STATUS: " + jda.getStatus().toString() + "\n" +
                        "GUILD LIST: " + jda.getGuilds().toString() + "\n";
            } else if(cmd[1].equalsIgnoreCase("restart")) {
                musicBot.restartShard(Integer.parseInt(cmd[2]));
            } else if(cmd[1].equalsIgnoreCase("start")) {
                musicBot.startShard(Integer.parseInt(cmd[2]));
            } else if(cmd[1].equalsIgnoreCase("stop")) {
                musicBot.stopShard(Integer.parseInt(cmd[2]));
            } else if(cmd[1].equalsIgnoreCase("disableshardscheck <true/false>")) {
                if(cmd[2].equalsIgnoreCase("true")) {
                    musicBot.getConfigManager().getConfig().setDisableShardsCheck(true);
                    returnString = "SENT ENABLE DISABLE SHARDS CHECK COMMAND";
                } else {
                    musicBot.getConfigManager().getConfig().setDisableShardsCheck(false);
                    returnString = "SENT DISABLE DISABLE SHARDS CHECK COMMAND";
                }
            }
        } else {
            returnString = "shards list\n" +
                    "shards info <shardId>\n" +
                    "shards restart <shardId>\n" +
                    "shards start <shardId>\n" +
                    "shards stop <shardId>\n";
        }
        return returnString;
    }

    private static String verboseCommand(MusicBot musicBot, String[] cmd) {
        String returnString = "";
        if(cmd.length >= 2) {
            if(cmd[1].equalsIgnoreCase("enable")) {
                if(cmd.length >= 3) {
                    if(cmd[2].equalsIgnoreCase("musicBot.getGMS()")) {
                        Console.setGMSLogging(true);
                        returnString = "ENABLED GMS VERBOSE OUTPUT";
                    } else if(cmd[2].equalsIgnoreCase("DB") || cmd[2].equalsIgnoreCase("DBM")) {
                        Console.setDBMLogging(true);
                        returnString = "ENABLED DB/DBM VERBOSE OUTPUT";
                    }
                }
            } else if(cmd[1].equalsIgnoreCase("disable")) {
                if(cmd.length >= 3) {
                    if(cmd[2].equalsIgnoreCase("GMS")) {
                        Console.setGMSLogging(false);
                        returnString = "DISABLED GMS VERBOSE OUTPUT";
                    } else if(cmd[2].equalsIgnoreCase("DB") || cmd[2].equalsIgnoreCase("DBM")) {
                        Console.setDBMLogging(false);
                        returnString = "DISABLED DB/DBM VERBOSE OUTPUT";
                    }
                }
            } else if(cmd[1].equalsIgnoreCase("info")) {
                returnString = "VERBOSE STATE:\n" +
                        "Guild Management System (GMS): " + Console.isGMSLogging() + "\n" +
                        "Database Manager (DB/DBM): " + Console.isDBMLogging() + "\n";
            }
        }
        return returnString;
    }

    private static String blacklistCommand(MusicBot musicBot, String[] cmd) {
        String returnString = "";

        if(cmd.length >= 3) {
            if(cmd[1].equalsIgnoreCase("global")) {
                if(cmd.length == 3) {
                    if(cmd[2].equalsIgnoreCase("list")) {
                        returnString = "GLOBAL BLACKLIST:\n" + musicBot.getDatabaseManager().getGlobalBlacklist().toString() + "\n";
                    } else if(cmd[2].equalsIgnoreCase("clear")) {
                        musicBot.getDatabaseManager().clearGlobalBlacklist();
                        returnString = "CLEARED GLOBAL BLACKLIST";
                    }
                } else if(cmd.length == 4) {
                    if(cmd[2].equalsIgnoreCase("add")) {
                        musicBot.getDatabaseManager().addToGlobalBlacklist(cmd[3]);
                        returnString = "ADDED LINK TO GLOBAL BLACKLIST";
                    } else if(cmd[2].equalsIgnoreCase("remove")) {
                        musicBot.getDatabaseManager().deleteFromGlobalBlacklist(cmd[3]);
                        returnString = "REMOVED LINK FROM GLOBAL BLACKLIST";
                    }
                }
            } else if(cmd[1].equalsIgnoreCase("guild")) {
                if(cmd.length == 4) {
                    if(cmd[3].equalsIgnoreCase("list")) {
                        returnString = "BLACKLIST:\n" + musicBot.getDatabaseManager().getBlacklist(cmd[2]).toString() + "\n";
                    } else if(cmd[3].equalsIgnoreCase("clear")) {
                        musicBot.getDatabaseManager().clearBlacklist(cmd[3]);
                        returnString = "CLEARED BLACKLIST";
                    }
                } else if(cmd.length == 5) {
                    if(cmd[3].equalsIgnoreCase("add")) {
                        musicBot.getDatabaseManager().addToBlacklist(cmd[2], cmd[4]);
                        returnString = "ADDED LINK TO BLACKLIST";
                    } else if(cmd[3].equalsIgnoreCase("remove")) {
                        musicBot.getDatabaseManager().deleteFromBlacklist(cmd[2], cmd[4]);
                        returnString = "REMOVED LINK FROM BLACKLIST";
                    }
                }
            }
        } else {
            returnString = "blacklist global add <link>\n" +
                    "blacklist global remove <link>\n" +
                    "blacklist global list\n" +
                    "blacklist global clear\n" +
                    "blacklist guild <guildId> add <link>\n" +
                    "blacklist guild <guildId> remove <link>\n" +
                    "blacklist guild <guildId> list\n" +
                    "blacklist guild <guildId> clear\n";
        }

        return returnString;
    }

    private static String keywordBlacklistCommand(MusicBot musicBot, String[] cmd) {
        String returnString = "";

        if(cmd.length >= 3) {
            if(cmd[1].equalsIgnoreCase("global")) {
                if(cmd.length == 3) {
                    if(cmd[2].equalsIgnoreCase("list")) {
                        returnString = "GLOBAL KEYWORD BLACKLIST:\n" + musicBot.getDatabaseManager().getGlobalKeywordBlacklist().toString() + "\n";
                    } else if(cmd[2].equalsIgnoreCase("clear")) {
                        musicBot.getDatabaseManager().clearGlobalKeywordBlacklist();
                        returnString = "CLEARED GLOBAL KEYWORD BLACKLIST";
                    }
                } else if(cmd.length == 4) {
                    if(cmd[2].equalsIgnoreCase("add")) {
                        musicBot.getDatabaseManager().addToGlobalKeywordBlacklist(cmd[3]);
                        returnString = "ADDED LINK TO GLOBAL KEYWORD BLACKLIST";
                    } else if(cmd[2].equalsIgnoreCase("remove")) {
                        musicBot.getDatabaseManager().deleteFromGlobalKeywordBlacklist(cmd[3]);
                        returnString = "REMOVED LINK FROM GLOBAL KEYWORD BLACKLIST";
                    }
                }
            } else if(cmd[1].equalsIgnoreCase("guild")) {
                if(cmd.length == 4) {
                    if(cmd[3].equalsIgnoreCase("list")) {
                        returnString = "KEYWORD BLACKLIST:\n" + musicBot.getDatabaseManager().getKeywordBlacklist(cmd[2]).toString() + "\n";
                    } else if(cmd[3].equalsIgnoreCase("clear")) {
                        musicBot.getDatabaseManager().clearKeywordBlacklist(cmd[3]);
                        returnString = "CLEARED KEYWORD BLACKLIST";
                    }
                } else if(cmd.length == 5) {
                    if(cmd[3].equalsIgnoreCase("add")) {
                        musicBot.getDatabaseManager().addToKeywordBlacklist(cmd[2], cmd[4].replace("%20", " "));
                        returnString = "ADDED LINK TO KEYWORD BLACKLIST";
                    } else if(cmd[3].equalsIgnoreCase("remove")) {
                        musicBot.getDatabaseManager().deleteFromKeywordBlacklist(cmd[2], cmd[4].replace("%20", " "));
                        returnString = "REMOVED LINK FROM KEYWORD BLACKLIST";
                    }
                }
            }
        } else {
            returnString = "keywordblacklist global add <link>\n" +
                    "keywordblacklist global remove <link>\n" +
                    "keywordblacklist global list\n" +
                    "keywordblacklist global clear\n" +
                    "keywordblacklist guild <guildId> add <link>\n" +
                    "keywordblacklist guild <guildId> remove <link>\n" +
                    "keywordblacklist guild <guildId> list\n" +
                    "keywordblacklist guild <guildId> clear\n" +
                    "Use %20 for space\n";
        }

        return returnString;
    }
    private static String artistBlacklistCommand(MusicBot musicBot, String[] cmd) {
        String returnString = "";

        if(cmd.length >= 3) {
            if(cmd[1].equalsIgnoreCase("global")) {
                if(cmd.length == 3) {
                    if(cmd[2].equalsIgnoreCase("list")) {
                        returnString = "GLOBAL ARTIST BLACKLIST:\n" + musicBot.getDatabaseManager().getGlobalArtistBlacklist().toString() + "\n";
                    } else if(cmd[2].equalsIgnoreCase("clear")) {
                        musicBot.getDatabaseManager().clearGlobalArtistBlacklist();
                        returnString = "CLEARED GLOBAL ARTIST BLACKLIST";
                    }
                } else if(cmd.length == 4) {
                    if(cmd[2].equalsIgnoreCase("add")) {
                        musicBot.getDatabaseManager().addToGlobalArtistBlacklist(cmd[3].replace("%20", " "));
                        returnString = "ADDED LINK TO GLOBAL ARTIST BLACKLIST";
                    } else if(cmd[2].equalsIgnoreCase("remove")) {
                        musicBot.getDatabaseManager().deleteFromGlobalArtistBlacklist(cmd[3].replace("%20", " "));
                        returnString = "REMOVED LINK FROM GLOBAL ARTIST BLACKLIST";
                    }
                }
            } else if(cmd[1].equalsIgnoreCase("guild")) {
                if(cmd.length == 4) {
                    if(cmd[3].equalsIgnoreCase("list")) {
                        returnString = "ARTIST BLACKLIST:\n" + musicBot.getDatabaseManager().getArtistBlacklist(cmd[2]).toString() + "\n";
                    } else if(cmd[3].equalsIgnoreCase("clear")) {
                        musicBot.getDatabaseManager().clearArtistBlacklist(cmd[3]);
                        returnString = "CLEARED ARTIST BLACKLIST";
                    }
                } else if(cmd.length == 5) {
                    if(cmd[3].equalsIgnoreCase("add")) {
                        musicBot.getDatabaseManager().addToArtistBlacklist(cmd[2], cmd[4].replace("%20", " "));
                        returnString = "ADDED LINK TO ARTIST BLACKLIST";
                    } else if(cmd[3].equalsIgnoreCase("remove")) {
                        musicBot.getDatabaseManager().deleteFromArtistBlacklist(cmd[2], cmd[4].replace("%20", " "));
                        returnString = "REMOVED LINK FROM ARTIST BLACKLIST";
                    }
                }
            }
        } else {
            returnString = "artistblacklist global add <link>\n" +
                    "artistblacklist global remove <link>\n" +
                    "artistblacklist global list\n" +
                    "artistblacklist global clear\n" +
                    "artistblacklist guild <guildId> add <link>\n" +
                    "artistblacklist guild <guildId> remove <link>\n" +
                    "artistblacklist guild <guildId> list\n" +
                    "artistblacklist guild <guildId> clear\n" +
                    "Use %20 for space\n";
        }

        return returnString;
    }

    private static String helpCommand(String[] cmd) {
        return "Help commands:\n" +
                "guild - Guild management\n" +
                "stop/shutdown - Shutdown the bot\n" +
                "invite - Get a corrent invite link\n" +
                "player - Manage guild music players\n" +
                "cmdreload [true] - Reload all slash commands\n" +
                "shard - Manage shards\n" +
                "verbose - Enable/disable verbose logging\n" +
                "blacklist - Manage url/identifier blacklist\n" +
                "keywordblacklist - Manage keyword blacklist\n" +
                "artistblacklist - Manage artist blacklist\n";
    }
}
