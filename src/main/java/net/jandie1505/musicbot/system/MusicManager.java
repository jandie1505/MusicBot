package net.jandie1505.musicbot.system;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.music.MusicPlayer;

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

    public static void joinVoiceChannel(VoiceChannel voiceChannel) {
        voiceChannel.getGuild().getAudioManager().setSendingHandler(getMusicPlayer(voiceChannel.getGuild().getId()).getAudioSendHandler());
        voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
    }

    public static void leaveVoiceChannel(Guild g) {
        if(isConnected(g)) {
            g.getAudioManager().closeAudioConnection();
        }
    }
    public static boolean isConnected(Guild g) {
        return g.getSelfMember().getVoiceState().inVoiceChannel();
    }

    public static void add(Guild g, String source) {
        getMusicPlayer(g.getId()).queue(source);
    }

    public static void add(Guild g, String source, SlashCommandEvent event) {
        getMusicPlayer(g.getId()).queue(source, event);
    }

    public static void remove(Guild g, int index) {
        getMusicPlayer(g.getId()).removeTrack(index);
    }

    public static void clear(Guild g) {
        getMusicPlayer(g.getId()).clearQueue();
    }

    public static List<AudioTrack> getQueue(Guild g) {
        return getMusicPlayer(g.getId()).getQueue();
    }

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

    public static void reloadPlayers() {
        for(String guildId : musicPlayers.keySet()) {
            Guild g = MusicBot.getShardManager().getGuildById(guildId);
            if(g == null) {
                musicPlayers.get(guildId).destroy();
                musicPlayers.remove(guildId);
            }
        }
    }
}
