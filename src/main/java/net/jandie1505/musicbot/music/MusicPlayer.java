package net.jandie1505.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private AudioPlayerManager playerManager;
    private AudioPlayer player;
    private AudioPlayerSendHandler playerSendHandler;
    private TrackScheduler trackScheduler;
    private List<AudioTrack> queue;

    public MusicPlayer() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer();
        playerSendHandler = new AudioPlayerSendHandler(player);
        trackScheduler = new TrackScheduler(this);
        player.addListener(trackScheduler);
        queue = new ArrayList<>();
    }

    // QUEUE
    public void queue(String source) {
        playerManager.loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                queue.add(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                queue.addAll(audioPlaylist.getTracks());
            }
            @Override public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    public void queue(String source, SlashCommandEvent event) {
        playerManager.loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                queue.add(audioTrack);
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription("Added " + audioTrack.getInfo().title + " [" + audioTrack.getInfo().author + "] to queue")
                        .setColor(Color.GREEN);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                queue.addAll(audioPlaylist.getTracks());
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription("Added " + audioPlaylist.getName() + " [" + audioPlaylist.getTracks().size() + " tracks] to queue")
                        .setColor(Color.GREEN);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            }

            @Override public void noMatches() {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(":warning:  Unknown source")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(":warning:  Load failed")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            }
        });
    }

    public void clearQueue() {
        queue.clear();
    }

    public List<AudioTrack> getQueue() {
        return queue;
    }

    public void nextTrack() {
        if(!queue.isEmpty()) {
            this.play(queue.get(0));
            queue.remove(0);
        }
    }
    public void nextTrack(AudioEventListener listener) {
        player.addListener(listener);
        if(!queue.isEmpty()) {
            this.play(queue.get(0));
            queue.remove(0);
        }
        player.removeListener(listener);
    }

    public void nextTrack(int index) {
        if(queue.size() >= index) {
            this.play(queue.get(index));
            queue.remove(index);
        }
    }
    public void nextTrack(int index, AudioEventListener listener) {
        player.addListener(listener);
        if(queue.size() >= index) {
            this.play(queue.get(index));
            queue.remove(index);
        }
        player.removeListener(listener);
    }

    public void removeTrack(int index) {
        if(queue.size() >= index) {
            queue.remove(index);
        }
    }

    // PLAYER
    public void play(AudioTrack audioTrack) {
        player.playTrack(audioTrack);
    }

    public void stop() {
        player.stopTrack();
    }

    public void setPause(boolean pause) {
        player.setPaused(pause);
    }

    public void setPause(boolean pause, AudioEventListener listener) {
        player.addListener(listener);
        player.setPaused(pause);
        player.removeListener(listener);
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public int getVolume() {
        return player.getVolume();
    }

    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    // GETTER
    public AudioPlayerSendHandler getAudioSendHandler() {
        return playerSendHandler;
    }

    // DESTROY
    public void destroy() {
        playerManager.shutdown();
        player.destroy();
    }
}
