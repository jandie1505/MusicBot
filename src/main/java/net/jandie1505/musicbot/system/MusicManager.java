package net.jandie1505.musicbot.system;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.music.MusicPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicManager {
    private static Map<String, MusicPlayer> musicPlayers;

    public static void init() {
        musicPlayers = new HashMap<>();
    }

    public static MusicPlayer getMusicPlayer(String guildId) {
        if(musicPlayers.containsKey(guildId)) {
            return musicPlayers.get(guildId);
        } else {
            MusicPlayer musicPlayer = new MusicPlayer();
            musicPlayers.put(guildId, musicPlayer);
            return musicPlayer;
        }
    }

    public static String getGuildIdFromMusicPlayer(MusicPlayer musicPlayer) {
        for(String guildId : musicPlayers.keySet()) {
            if(musicPlayers.get(guildId) == musicPlayer) {
                return guildId;
            }
        }
        return "";
    }

    // CONNECTION
    public static void connect(VoiceChannel voiceChannel) {
        voiceChannel.getGuild().getAudioManager().setSendingHandler(getMusicPlayer(voiceChannel.getGuild().getId()).getAudioSendHandler());
        voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
    }
    public static void disconnect(Guild g) {
        if(isConnected(g)) {
            g.getAudioManager().closeAudioConnection();
        }
    }
    public static boolean isConnected(Guild g) {
        return g.getSelfMember().getVoiceState().inVoiceChannel();
    }

    public static void joinVoiceChannel(VoiceChannel voiceChannel) {
        connect(voiceChannel);
    }
    public static void leaveVoiceChannel(Guild g) {
        disconnect(g);
    }

    // QUEUE
    public static void add(Guild g, String source, boolean startafterload) {
        getMusicPlayer(g.getId()).queue(source, startafterload);
    }
    public static void add(Guild g, String source, SlashCommandEvent event, boolean startafterload) {
        getMusicPlayer(g.getId()).queue(source, event, startafterload);
    }
    public static void remove(Guild g, int index) {
        getMusicPlayer(g.getId()).removeTrack(index);
    }
    public static void move(Guild g, int from, int to) {
        getMusicPlayer(g.getId()).moveTrack(from, to);
    }
    public static void clear(Guild g) {
        getMusicPlayer(g.getId()).clearQueue();
    }
    public static List<AudioTrack> getQueue(Guild g) {
        return getMusicPlayer(g.getId()).getQueue();
    }
    public static void shuffle(Guild g) {
        getMusicPlayer(g.getId()).shuffle();
    }

    // PLAYER
    public static void setPause(Guild g, boolean pause) {
        getMusicPlayer(g.getId()).setPause(pause);
    }
    public static void setPause(Guild g, boolean pause, AudioEventListener listener) {
        getMusicPlayer(g.getId()).setPause(pause, listener);
    }
    public static boolean isPaused(Guild g) {
        return getMusicPlayer(g.getId()).isPaused();
    }

    public static AudioTrack getPlayingTrack(Guild g) {
        return getMusicPlayer(g.getId()).getPlayingTrack();
    }

    public static void stop(Guild g) {
        getMusicPlayer(g.getId()).stop();
    }

    public static void next(Guild g) {
        getMusicPlayer(g.getId()).nextTrack();
    }
    public static void next(Guild g, AudioEventListener listener) {
        getMusicPlayer(g.getId()).nextTrack(listener);
    }
    public static void next(Guild g, int position) {
        getMusicPlayer(g.getId()).nextTrack(position);
    }
    public static void next(Guild g, int position, AudioEventListener listener) {
        getMusicPlayer(g.getId()).nextTrack(position, listener);
    }

    public static void playnow(Guild g, String source) {
        getMusicPlayer(g.getId()).playnow(source);
    }
    public static void playnow(Guild g, String source, SlashCommandEvent event) {
        getMusicPlayer(g.getId()).playnow(source, event);
    }

    public static void setVolume(Guild g, int volume) {
        getMusicPlayer(g.getId()).setVolume(volume);
    }
    public static int getVolume(Guild g) {
        return getMusicPlayer(g.getId()).getVolume();
    }

    // SKIPVOTES
    public static void addSkipvote(Guild g, Member m) {
        if (!getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            getMusicPlayer(g.getId()).createSkipvoteManager();
        }
        getMusicPlayer(g.getId()).getSkipvoteManager().addSkipvote(m);
    }

    public static void removeSkipvote(Guild g, Member m) {
        if(getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            getMusicPlayer(g.getId()).getSkipvoteManager().removeSkipvote(m);
        }
    }

    public static List<Member> getSkipvotes(Guild g) {
        List<Member> returnList = new ArrayList<>();

        if(getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            returnList.addAll(getMusicPlayer(g.getId()).getSkipvoteManager().getSkipvotes());
        }

        return returnList;
    }

    public static int getVoteCount(Guild g) {
        if(getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            return getMusicPlayer(g.getId()).getSkipvoteManager().getVoteCount();
        }
        return 0;
    }

    public static int getRequiredVotes(Guild g) {
        if(getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            return getMusicPlayer(g.getId()).getSkipvoteManager().getRequiredVotes();
        }
        return 0;
    }


    // PLAYER MANAGEMENT
    public static void reloadPlayers() {
        for(String guildId : musicPlayers.keySet()) {
            Guild g = MusicBot.getShardManager().getGuildById(guildId);
            if(g == null) {
                musicPlayers.get(guildId).destroy();
                musicPlayers.remove(guildId);
            } else {
                if(!g.getSelfMember().getVoiceState().inVoiceChannel()) {
                    musicPlayers.get(guildId).destroy();
                    musicPlayers.remove(guildId);
                }
            }
        }
    }

    public static void removePlayer(String guildId) {
        if(musicPlayers.containsKey(guildId)) {
            musicPlayers.get(guildId).destroy();
        }
        musicPlayers.remove(guildId);
    }
}
