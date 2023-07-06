package net.jandie1505.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.jandie1505.musicbot.MusicBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicManager {

    private final MusicBot musicBot;
    private final Map<String, MusicPlayer> musicPlayers;

    public MusicManager(MusicBot musicBot) {
        this.musicBot = musicBot;
        this.musicPlayers = new HashMap<>();
    }

    // CONNECTION

    public boolean connect(AudioChannel audioChannel) {

        try {

            audioChannel.getGuild().getAudioManager().setSendingHandler(this.getMusicPlayer(audioChannel.getGuild().getId()).getAudioSendHandler());
            audioChannel.getGuild().getAudioManager().openAudioConnection(audioChannel);

            return true;

        } catch(Exception e) {
            return false;
        }

    }

    public void disconnect(Guild g) {

        if(isConnected(g)) {

            g.getAudioManager().closeAudioConnection();
            removePlayer(g.getId());

        }

    }

    public boolean isConnected(Guild g) {
        return g.getSelfMember().getVoiceState().inAudioChannel();
    }

    // QUEUE

    public void add(Guild g, String source, boolean startafterload) {
        getMusicPlayer(g.getId()).enqueue(source, startafterload);
    }

    public void add(Guild g, String source, SlashCommandInteractionEvent event, boolean startafterload) {
        getMusicPlayer(g.getId()).enqueue(source, event, startafterload);
    }

    public void remove(Guild g, int index) {
        getMusicPlayer(g.getId()).removeTrack(index);
    }

    public void move(Guild g, int from, int to) {
        getMusicPlayer(g.getId()).moveTrack(from, to);
    }

    public void clear(Guild g) {
        getMusicPlayer(g.getId()).clearQueue();
    }

    public List<AudioTrack> getQueue(Guild g) {
        return getMusicPlayer(g.getId()).getQueue();
    }

    public void shuffle(Guild g) {
        getMusicPlayer(g.getId()).shuffle();
    }

    // PLAYER

    public void setPause(Guild g, boolean pause) {
        getMusicPlayer(g.getId()).setPause(pause);
    }

    public void setPause(Guild g, boolean pause, AudioEventListener listener) {
        getMusicPlayer(g.getId()).setPause(pause, listener);
    }

    public boolean isPaused(Guild g) {
        return getMusicPlayer(g.getId()).isPaused();
    }

    public AudioTrack getPlayingTrack(Guild g) {
        return getMusicPlayer(g.getId()).getPlayingTrack();
    }

    public void stop(Guild g) {
        getMusicPlayer(g.getId()).stop();
    }

    public void next(Guild g) {
        getMusicPlayer(g.getId()).nextTrack();
    }

    public void next(Guild g, AudioEventListener listener) {
        getMusicPlayer(g.getId()).nextTrack(listener);
    }

    public void next(Guild g, int position) {
        getMusicPlayer(g.getId()).nextTrack(position);
    }

    public void next(Guild g, int position, AudioEventListener listener) {
        getMusicPlayer(g.getId()).nextTrack(position, listener);
    }

    public void playnow(Guild g, String source) {
        getMusicPlayer(g.getId()).playnow(source);
    }

    public void playnow(Guild g, String source, SlashCommandInteractionEvent event) {
        getMusicPlayer(g.getId()).playnow(source, event);
    }

    public void setVolume(Guild g, int volume) {
        getMusicPlayer(g.getId()).setVolume(volume);
    }

    public int getVolume(Guild g) {
        return getMusicPlayer(g.getId()).getVolume();
    }

    // SKIPVOTES
    public void addSkipvote(Guild g, Member m) {
        if (!getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            getMusicPlayer(g.getId()).createSkipvoteManager();
        }
        getMusicPlayer(g.getId()).getSkipvoteManager().addSkipvote(m);
    }

    public void removeSkipvote(Guild g, Member m) {
        if(getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            getMusicPlayer(g.getId()).getSkipvoteManager().removeSkipvote(m);
        }
    }

    public List<Member> getSkipvotes(Guild g) {
        List<Member> returnList = new ArrayList<>();

        if(getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            returnList.addAll(getMusicPlayer(g.getId()).getSkipvoteManager().getSkipvotes());
        }

        return returnList;
    }

    public int getVoteCount(Guild g) {
        if(getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            return getMusicPlayer(g.getId()).getSkipvoteManager().getVoteCount();
        }
        return 0;
    }

    public int getRequiredVotes(Guild g) {
        if(getMusicPlayer(g.getId()).hasSkipvoteManaer()) {
            return getMusicPlayer(g.getId()).getSkipvoteManager().getRequiredVotes();
        }
        return 0;
    }

    // PLAYER MANAGEMENT

    public void reload() {

        for (String guildId : musicPlayers.keySet()) {

            if (this.musicBot.getShardManager() == null) {
                this.removePlayer(guildId);
                continue;
            }

            Guild g = this.musicBot.getShardManager().getGuildById(guildId);

            if (g == null) {
                this.removePlayer(guildId);
                continue;
            }

        }

    }

    public MusicPlayer getMusicPlayer(String guildId) {

        MusicPlayer musicPlayer = this.musicPlayers.get(guildId);

        if (musicPlayer == null) {

            musicPlayer = new MusicPlayer(this);
            this.musicPlayers.put(guildId, musicPlayer);

        }

        return musicPlayer;
    }

    public String getGuildIdFromMusicPlayer(MusicPlayer musicPlayer) {

        for (String guildId : List.copyOf(this.musicPlayers.keySet())) {

            if (this.musicPlayers.get(guildId) == musicPlayer) {
                return guildId;
            }

        }

        return "";
    }

    public void removePlayer(String guildId) {

        MusicPlayer musicPlayer = this.musicPlayers.get(guildId);

        if (musicPlayer == null) {
            return;
        }

        musicPlayer.destroy();
        this.musicPlayers.remove(guildId);

    }

    public MusicBot getMusicBot() {
        return this.musicBot;
    }
}
