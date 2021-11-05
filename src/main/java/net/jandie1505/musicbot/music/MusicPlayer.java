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
import net.jandie1505.musicbot.system.GMS;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MusicPlayer {
    private AudioPlayerManager playerManager;
    private AudioPlayer player;
    private AudioPlayerSendHandler playerSendHandler;
    private TrackScheduler trackScheduler;
    private List<AudioTrack> queue;
    private SkipvoteManager skipvoteManager;

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
    public void queue(String source, boolean startafterload) {
        playerManager.loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                queue.add(audioTrack);
                if(startafterload) {
                    nextTrack();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                queue.addAll(audioPlaylist.getTracks());
                if(startafterload) {
                    nextTrack();
                }
            }
            @Override public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    public void queue(String source, SlashCommandEvent event, boolean startafterload) {
        playerManager.loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                if(!GMS.isBlacklisted(event.getGuild(), event.getMember(), audioTrack.getInfo().uri)) {
                    queue.add(audioTrack);
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription("Added " + audioTrack.getInfo().title + " [" + audioTrack.getInfo().author + "] to queue")
                            .setColor(Color.GREEN);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    if(startafterload) {
                        nextTrack();
                    }
                } else {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":warning:  This track is blacklisted")
                            .setColor(Color.RED);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                boolean blacklisted = false;
                for(AudioTrack track : audioPlaylist.getTracks()) {
                    if(!GMS.isBlacklisted(event.getGuild(), event.getMember(), track.getInfo().uri)) {
                        queue.add(track);
                    } else {
                        blacklisted = true;
                    }
                }
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription("Added " + audioPlaylist.getName() + " [" + audioPlaylist.getTracks().size() + " tracks] to queue")
                        .setColor(Color.GREEN);
                if(blacklisted) {
                    embedBuilder.setDescription("Added " + audioPlaylist.getName() + " [" + audioPlaylist.getTracks().size() + " tracks] to queue (some of the tracks are blacklisted and could not be added)");
                }
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                if(startafterload && !queue.isEmpty()) {
                    nextTrack();
                }
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

    public void addAudioTrack(AudioTrack track) {
        queue.add(track);
    }

    public void addAudioTrackList(List<AudioTrack> audioTrackList) {
        queue.addAll(audioTrackList);
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
        } else {
            this.stop();
        }
    }
    public void nextTrack(AudioEventListener listener) {
        player.addListener(listener);
        if(!queue.isEmpty()) {
            this.play(queue.get(0));
            queue.remove(0);
        } else {
            this.stop();
        }
        player.removeListener(listener);
    }

    public void nextTrack(int index) {
        if(queue.size() >= index) {
            this.play(queue.get(index));
            queue.subList(0, index + 1).clear();
        } else {
            this.stop();
        }
    }
    public void nextTrack(int index, AudioEventListener listener) {
        player.addListener(listener);
        if(queue.size() >= index) {
            this.play(queue.get(index));
            queue.subList(0, index + 1).clear();
        } else {
            this.stop();
        }
        player.removeListener(listener);
    }

    public void removeTrack(int index) {
        if(queue.size() >= index) {
            queue.remove(index);
        }
    }

    public void moveTrack(int from, int to) {
        if(from < queue.size()) {
            if(to < queue.size()) {
                queue.add(to, queue.remove(from));
            } else {
                if(queue.size()-1 >= 0) {
                    queue.add(queue.size()-1, queue.remove(from));
                }
            }
        }
    }

    public void shuffle() {
        if(!queue.isEmpty()) {
            Collections.shuffle(queue);
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
        if(System.getProperty("os.arch").equalsIgnoreCase("amd64")) {
            player.setVolume(volume);
        }
    }

    public int getVolume() {
        return player.getVolume();
    }

    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    public void playnow(String source) {
        playerManager.loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                play(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {}

            @Override public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException e) {}
        });
    }
    public void playnow(String source, SlashCommandEvent event) {
        playerManager.loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                if(!GMS.isBlacklisted(event.getGuild(), event.getMember(), audioTrack.getInfo().uri)) {
                    play(audioTrack);
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription("Playing " + audioTrack.getInfo().title + " [" + audioTrack.getInfo().author + "] now")
                            .setColor(Color.GREEN);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                } else {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":warning:  This track is blacklisted")
                            .setColor(Color.RED);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(":warning:  Playlists are not supported here")
                        .setColor(Color.RED);
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

    // SKIPVOTES
    public void createSkipvoteManager() {
        if(skipvoteManager == null) {
            this.skipvoteManager = new SkipvoteManager(this, 300);
        }
    }

    public void destroySkipvoteManager() {
        this.skipvoteManager = null;
    }

    public SkipvoteManager getSkipvoteManager() {
        return skipvoteManager;
    }

    public boolean hasSkipvoteManaer() {
        return Objects.nonNull(skipvoteManager);
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
