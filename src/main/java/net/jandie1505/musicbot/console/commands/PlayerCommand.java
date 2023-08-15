package net.jandie1505.musicbot.console.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.CommandExecutor;
import net.jandie1505.musicbot.music.MusicPlayer;
import net.jandie1505.musicbot.utilities.BotStatus;

import java.util.List;

public class PlayerCommand implements CommandExecutor {
    private final MusicBot musicBot;

    public PlayerCommand(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public String onCommand(String command, String[] args) {

        if (args.length < 1) {
            return "Player command help:\n" +
                    "player status <guildId>\n" +
                    "player voicestate <guildId>\n" +
                    "player connect <guildId> <channelId>\n" +
                    "player disconnect <guildId>\n" +
                    "player play <guildId> <source>\n" +
                    "player pause <guildId> [true/false]\n" +
                    "player stop <guildId>\n" +
                    "player queue <guildId> [subcommand]\n" +
                    "player volume <guildId> [amount]\n" +
                    "player remove <guildId>\n" +
                    "player list\n" +
                    "player cleanup\n";
        }

        try {

            switch (args[0]) {
                case "status" -> {

                    if (args.length < 2) {
                        return "Usage: player status <guildId>";
                    }

                    MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(Long.parseLong(args[1]));

                    String info = "PLAYER INFO:";

                    if (player.getPlayingTrack() != null) {
                        info = info + "\nPlaying track:" +
                                "\n - URI:" + player.getPlayingTrack().getInfo().uri +
                                "\n - Name: " + player.getPlayingTrack().getInfo().title +
                                "\n - Author: " + player.getPlayingTrack().getInfo().author +
                                "\n - Position: " + player.getPlayingTrack().getPosition() + " / " + player.getPlayingTrack().getInfo().length +
                                "\n - Stream: " + player.getPlayingTrack().getInfo().isStream +
                                "\n - Identifier: " + player.getPlayingTrack().getIdentifier() +
                                "\n - State: " + player.getPlayingTrack().getState();
                    }

                    info = info + "\nPaused: " + player.isPaused();

                    info = info + "\nQueue: " + player.getQueue().size() + " entries";

                    info = info + "\nVolume: " + player.getVolume();

                    return info;
                }
                case "voicestate" -> {

                    if (this.musicBot.getBotStatus() != BotStatus.ACTIVE) {
                        return "Command disabled because bot is not started";
                    }

                    if (args.length < 2) {
                        return "Usage: player voicestate <guildId>";
                    }

                    Guild g = this.musicBot.getShardManager().getGuildById(Long.parseLong(args[1]));

                    if (g == null) {
                        return "Guild not found";
                    }

                    GuildVoiceState voiceState = g.getSelfMember().getVoiceState();

                    if (voiceState == null) {
                        return "Unknown voice state";
                    }

                    if (voiceState.inAudioChannel()) {
                        return "Connected to " + voiceState.getChannel().getIdLong() + " on guild " + g.getIdLong();
                    } else {
                        return "Not connected";
                    }

                }
                case "connect" -> {

                    if (this.musicBot.getBotStatus() != BotStatus.ACTIVE) {
                        return "Command disabled because bot is not started";
                    }

                    if (args.length < 3) {
                        return "Usage: player connect <guildId> <channelId>";
                    }

                    Guild g = this.musicBot.getShardManager().getGuildById(Long.parseLong(args[1]));

                    if (g == null) {
                        return "Guild not found";
                    }

                    AudioChannel channel = g.getVoiceChannelById(Long.parseLong(args[2]));

                    if (channel == null) {
                        channel = g.getStageChannelById(Long.parseLong(args[2]));
                    }

                    if (channel == null) {
                        return "Channel does not exist";
                    }

                    boolean success = this.musicBot.getMusicManager().connect(channel);

                    if (success) {
                        return "Successfully connected to " + channel.getIdLong();
                    } else {
                        return "Connection to " + channel.getIdLong() + " failed";
                    }
                }
                case "disconnect" -> {

                    if (this.musicBot.getBotStatus() != BotStatus.ACTIVE) {
                        return "Command disabled because bot is not started";
                    }

                    if (args.length < 2) {
                        return "Usage: player disconnect <guildId>";
                    }

                    Guild g = this.musicBot.getShardManager().getGuildById(Long.parseLong(args[1]));

                    if (g == null) {
                        return "Guild not found";
                    }

                    this.musicBot.getMusicManager().disconnect(g);
                    return "Disconnected";
                }
                case "play" -> {

                    if (args.length < 3) {
                        return "Usage: player play <guildId> <source>";
                    }

                    MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(Long.parseLong(args[1]));

                    String source = "";

                    for (int i = 2; i < args.length; i++) {
                        source = source + args[i];
                    }

                    player.playnow(source);
                    return "Success";
                }
                case "pause" -> {

                    if (args.length < 2) {
                        return "Usage: player pause <guildId> [status]";
                    }

                    MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(Long.parseLong(args[1]));

                    if (args.length < 3) {
                        return "Current pause status: " + player.isPaused();
                    }

                    player.setPause(Boolean.parseBoolean(args[2]));
                    return "Pause status set to " + Boolean.parseBoolean(args[2]);
                }
                case "stop" -> {

                    if (args.length < 2) {
                        return "Usage: player stop <guildId>";
                    }

                    MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(Long.parseLong(args[1]));

                    player.stop();
                    return "Player stopped";
                }
                case "volume" -> {

                    if (args.length < 2) {
                        return "Usage: player volume <guildId> [volume]";
                    }

                    MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(Long.parseLong(args[1]));

                    if (args.length < 3) {
                        return "Current volume: " + player.getVolume();
                    }

                    player.setVolume(Integer.parseInt(args[2]));
                    return "Set volume to " + Integer.parseInt(args[2]);
                }
                case "remove" -> {

                    if (args.length < 2) {
                        return "Usage: player remove <guildId>";
                    }

                    this.musicBot.getMusicManager().removePlayer(Long.parseLong(args[1]));
                    return "Player removed";
                }
                case "list" -> {

                    String list = "| GUILD ID | TRACK | PAUSE |";

                    for (Long guildId : this.musicBot.getMusicManager().getPlayers().keySet()) {
                        MusicPlayer player = this.musicBot.getMusicManager().getPlayers().get(guildId);

                        if (player == null) {
                            continue;
                        }

                        list = list + "\n| " + guildId + " | " + (player.getPlayingTrack() != null) + " | " + player.isPaused() + " |";

                    }

                    return list;
                }
                case "cleanup" -> {
                    this.musicBot.getMusicManager().reloadPlayers();
                    return "Cleaned up players";
                }
                case "queue" -> {

                    if (args.length < 3) {
                        return "Player queue command help:\n" +
                                "player queue <guildId> add <source>\n" +
                                "player queue <guildId> remove <index>\n" +
                                "player queue <guildId> clear\n" +
                                "player queue <guildId> list\n" +
                                "player queue <guildId> move <index> <otherIndex>";
                    }

                    MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(Long.parseLong(args[1]));

                    switch (args[2]) {
                        case "add" -> {

                            if (args.length < 4) {
                                return "Usage: player queue <guildId> add <source>";
                            }

                            String source = "";

                            for (int i = 3; i < args.length; i++) {
                                source = source + args[i];
                            }

                            player.enqueue(source, false);
                            return "Enqueued";
                        }
                        case "remove" -> {

                            if (args.length < 4) {
                                return "Usage: player queue <guildId> remove <index>";
                            }

                            player.removeTrack(Integer.parseInt(args[3]));
                            return "Removed";
                        }
                        case "clear" -> {
                            player.clearQueue();
                            return "Queue cleared";
                        }
                        case "list" -> {

                            String list = "| INDEX | TITLE | AUTHOR | URI |";

                            for (int i = 0; i < player.getQueue().size(); i++) {
                                AudioTrack track = player.getQueue().get(i);

                                list = list + "\n| " + i + " | " + track.getInfo().title + " | " + track.getInfo().author + " | " + track.getInfo().uri + " |";
                            }

                            for (AudioTrack track : List.copyOf(player.getQueue())) {

                            }

                            return list;
                        }
                        case "move" -> {

                            if (args.length < 5) {
                                return "Usage: player queue <guildId> move <index> <otherIndex>";
                            }

                            player.moveTrack(Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                            return "Track moved";
                        }
                        default -> {
                            return "Unknown subcommand";
                        }
                    }

                }
                default -> {
                    return "Unknown subcommand";
                }
            }

        } catch (IllegalArgumentException e) {
            return "Illegal argument";
        }
    }
}
