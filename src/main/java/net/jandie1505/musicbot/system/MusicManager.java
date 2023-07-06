package net.jandie1505.musicbot.system;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.music.MusicPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicManager {

    private final MusicBot musicBot;
    private final Map<String, MusicPlayer> musicPlayers;
    private final Map<String, Integer> playerExpiration;

    public MusicManager(MusicBot musicBot) {
        this.musicBot = musicBot;
        this.musicPlayers = new HashMap<>();
        this.playerExpiration = new HashMap<>();
    }

    // CONNECTION
    public boolean connect(AudioChannel audioChannel) {
        try {
            audioChannel.getGuild().getAudioManager().setSendingHandler(getMusicPlayer(audioChannel.getGuild().getId()).getAudioSendHandler());
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

    public void joinVoiceChannel(VoiceChannel voiceChannel) {
        connect(voiceChannel);
    }
    public void leaveVoiceChannel(Guild g) {
        disconnect(g);
    }

    // QUEUE
    public void add(Guild g, String source, boolean startafterload) {
        getMusicPlayer(g.getId()).queue(source, startafterload);
    }
    public void add(Guild g, String source, SlashCommandInteractionEvent event, boolean startafterload) {
        getMusicPlayer(g.getId()).queue(source, event, startafterload);
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
        for(String guildId : musicPlayers.keySet()) {
            Guild g = this.musicBot.getShardManager().getGuildById(guildId);
            if(g == null) {
                removePlayer(guildId);
            } else {
                if(g.getSelfMember().getVoiceState().inAudioChannel()) {
                    /*if(!playerExpiration.containsKey(guildId)) {
                        playerExpiration.put(guildId, 900);
                    }
                    List<Member> memberList = g.getSelfMember().getVoiceState().getChannel().getMembers();
                    memberList.remove(g.getSelfMember());
                    if(memberList.isEmpty()) {
                        if(playerExpiration.get(guildId) > 0) {
                            playerExpiration.put(guildId, playerExpiration.get(guildId)-1);
                        } else if(playerExpiration.get(guildId) == 0) {
                            disconnect(g);
                        }
                    } else {
                        playerExpiration.put(guildId, 900);
                    }
                     */
                } else {
                    removePlayer(guildId);
                }
            }
        }
    }

    public void playerExpiration() {
        for(String guildId : musicPlayers.keySet()) {
            MusicPlayer musicPlayer = musicPlayers.get(guildId);

        }
    }

    public MusicPlayer getMusicPlayer(String guildId) {
        if(musicPlayers.containsKey(guildId)) {
            return musicPlayers.get(guildId);
        } else {
            MusicPlayer musicPlayer = new MusicPlayer(this);
            musicPlayers.put(guildId, musicPlayer);
            return musicPlayer;
        }
    }

    public String getGuildIdFromMusicPlayer(MusicPlayer musicPlayer) {
        for(String guildId : musicPlayers.keySet()) {
            if(musicPlayers.get(guildId) == musicPlayer) {
                return guildId;
            }
        }
        return "";
    }

    public void removePlayer(String guildId) {
        if(musicPlayers.containsKey(guildId)) {
            musicPlayers.get(guildId).destroy();
        }
        if(playerExpiration.containsKey(guildId)) {
            playerExpiration.remove(guildId);
        }
        musicPlayers.remove(guildId);
    }

    public MusicBot getMusicBot() {
        return this.musicBot;
    }
}
