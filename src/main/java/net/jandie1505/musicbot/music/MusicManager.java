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
    private final Map<Long, MusicPlayer> musicPlayers;

    public MusicManager(MusicBot musicBot) {
        this.musicBot = musicBot;
        this.musicPlayers = new HashMap<>();
    }

    // CONNECTION

    public boolean connect(AudioChannel audioChannel) {

        try {

            audioChannel.getGuild().getAudioManager().setSendingHandler(this.getMusicPlayer(audioChannel.getGuild().getIdLong()).getAudioSendHandler());
            audioChannel.getGuild().getAudioManager().openAudioConnection(audioChannel);

            return true;

        } catch(Exception e) {
            return false;
        }

    }

    public void disconnect(Guild g) {

        if(isConnected(g)) {

            g.getAudioManager().closeAudioConnection();
            removePlayer(g.getIdLong());

        }

    }

    public boolean isConnected(Guild g) {
        return g.getSelfMember().getVoiceState().inAudioChannel();
    }

    // QUEUE

    public void add(Guild g, String source, boolean startafterload) {
        getMusicPlayer(g.getIdLong()).enqueue(source, startafterload);
    }

    public void add(Guild g, String source, SlashCommandInteractionEvent event, boolean startafterload) {
        getMusicPlayer(g.getIdLong()).enqueue(source, event, startafterload);
    }

    public void remove(Guild g, int index) {
        getMusicPlayer(g.getIdLong()).removeTrack(index);
    }

    public void move(Guild g, int from, int to) {
        getMusicPlayer(g.getIdLong()).moveTrack(from, to);
    }

    public void clear(Guild g) {
        getMusicPlayer(g.getIdLong()).clearQueue();
    }

    public List<AudioTrack> getQueue(Guild g) {
        return getMusicPlayer(g.getIdLong()).getQueue();
    }

    public void shuffle(Guild g) {
        getMusicPlayer(g.getIdLong()).shuffle();
    }

    // PLAYER

    public void setPause(Guild g, boolean pause) {
        getMusicPlayer(g.getIdLong()).setPause(pause);
    }

    public void setPause(Guild g, boolean pause, AudioEventListener listener) {
        getMusicPlayer(g.getIdLong()).setPause(pause, listener);
    }

    public boolean isPaused(Guild g) {
        return getMusicPlayer(g.getIdLong()).isPaused();
    }

    public AudioTrack getPlayingTrack(Guild g) {
        return getMusicPlayer(g.getIdLong()).getPlayingTrack();
    }

    public void stop(Guild g) {
        getMusicPlayer(g.getIdLong()).stop();
    }

    public void next(Guild g) {
        getMusicPlayer(g.getIdLong()).nextTrack();
    }

    public void next(Guild g, AudioEventListener listener) {
        getMusicPlayer(g.getIdLong()).nextTrack(listener);
    }

    public void next(Guild g, int position) {
        getMusicPlayer(g.getIdLong()).nextTrack(position);
    }

    public void next(Guild g, int position, AudioEventListener listener) {
        getMusicPlayer(g.getIdLong()).nextTrack(position, listener);
    }

    public void playnow(Guild g, String source) {
        getMusicPlayer(g.getIdLong()).playnow(source);
    }

    public void playnow(Guild g, String source, SlashCommandInteractionEvent event) {
        getMusicPlayer(g.getIdLong()).playnow(source, event);
    }

    public void setVolume(Guild g, int volume) {
        getMusicPlayer(g.getIdLong()).setVolume(volume);
    }

    public int getVolume(Guild g) {
        return getMusicPlayer(g.getIdLong()).getVolume();
    }

    // SKIPVOTES
    public void addSkipvote(Guild g, Member m) {
        if (!getMusicPlayer(g.getIdLong()).hasSkipvoteManaer()) {
            getMusicPlayer(g.getIdLong()).createSkipvoteManager();
        }
        getMusicPlayer(g.getIdLong()).getSkipvoteManager().addSkipvote(m);
    }

    public void removeSkipvote(Guild g, Member m) {
        if(getMusicPlayer(g.getIdLong()).hasSkipvoteManaer()) {
            getMusicPlayer(g.getIdLong()).getSkipvoteManager().removeSkipvote(m);
        }
    }

    public List<Member> getSkipvotes(Guild g) {
        List<Member> returnList = new ArrayList<>();

        if(getMusicPlayer(g.getIdLong()).hasSkipvoteManaer()) {
            returnList.addAll(getMusicPlayer(g.getIdLong()).getSkipvoteManager().getSkipvotes());
        }

        return returnList;
    }

    public int getVoteCount(Guild g) {
        if(getMusicPlayer(g.getIdLong()).hasSkipvoteManaer()) {
            return getMusicPlayer(g.getIdLong()).getSkipvoteManager().getVoteCount();
        }
        return 0;
    }

    public int getRequiredVotes(Guild g) {
        if(getMusicPlayer(g.getIdLong()).hasSkipvoteManaer()) {
            return getMusicPlayer(g.getIdLong()).getSkipvoteManager().getRequiredVotes();
        }
        return 0;
    }

    // PLAYER MANAGEMENT

    public void reload() {

        for (long guildId : musicPlayers.keySet()) {

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

    public MusicPlayer getMusicPlayer(long guildId) {

        MusicPlayer musicPlayer = this.musicPlayers.get(guildId);

        if (musicPlayer == null) {

            musicPlayer = new MusicPlayer(this);
            this.musicPlayers.put(guildId, musicPlayer);

        }

        return musicPlayer;
    }

    public long getGuildIdFromMusicPlayer(MusicPlayer musicPlayer) {

        for (long guildId : List.copyOf(this.musicPlayers.keySet())) {

            if (this.musicPlayers.get(guildId) == musicPlayer) {
                return guildId;
            }

        }

        return -1;
    }

    public void removePlayer(long guildId) {

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
