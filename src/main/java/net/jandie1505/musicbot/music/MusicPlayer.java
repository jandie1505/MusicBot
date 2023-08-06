package net.jandie1505.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MusicPlayer {
    private final MusicManager musicManager;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final AudioPlayerSendHandler playerSendHandler;
    private final TrackScheduler trackScheduler;
    private final List<AudioTrack> queue;
    private SkipvoteManager skipvoteManager;

    public MusicPlayer(MusicManager musicManager) {
        this.musicManager = musicManager;
        this.playerManager = new DefaultAudioPlayerManager();
        this.player = playerManager.createPlayer();
        this.playerSendHandler = new AudioPlayerSendHandler(this.player);
        this.trackScheduler = new TrackScheduler(this);
        this.queue = Collections.synchronizedList(new ArrayList<>());

        AudioSourceManagers.registerRemoteSources(this.playerManager);
        this.player.addListener(this.trackScheduler);
    }

    // QUEUE

    public void enqueue(String source, boolean startafterload) {
        this.playerManager.loadItem(source, new AudioLoadResultHandler() {

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

    public MusicEqueuedResponse enqueueWithResponse(String source, boolean startafterload) {
        CountDownLatch latch = new CountDownLatch(1);
        final MusicEqueuedResponse[] response = {null};

        this.playerManager.loadItem(source, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {

                queue.add(audioTrack);

                if(startafterload) {
                    nextTrack();
                }

                response[0] = new MusicEqueuedResponse(audioTrack.getInfo().title);
                latch.countDown();

            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

                queue.addAll(audioPlaylist.getTracks());

                if(startafterload) {
                    nextTrack();
                }

                response[0] = new MusicEqueuedResponse(audioPlaylist.getTracks().size());
                latch.countDown();

            }

            @Override public void noMatches() {
                latch.countDown();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                latch.countDown();
            }

        });

        try {
            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
            // ignored
        }

        return response[0];
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

        if (from < 0 || to < 0) {
            return;
        }

        if (from >= queue.size()) {
            return;
        }

        if (to < queue.size()) {
            queue.add(to, queue.remove(from));
        } else {
            queue.add(queue.size() - 1, queue.remove(from));
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

    public MusicEqueuedResponse playnowWithResponse(String source) {
        CountDownLatch latch = new CountDownLatch(1);
        final MusicEqueuedResponse[] response = {null};

        playerManager.loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {

                play(audioTrack);

                response[0] = new MusicEqueuedResponse(audioTrack.getInfo().title);
                latch.countDown();

            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {}

            @Override
            public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException e) {}
        });

        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // ignored
        }

        return response[0];
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
    public MusicManager getMusicManager() {
        return this.musicManager;
    }

    public AudioPlayerSendHandler getAudioSendHandler() {
        return playerSendHandler;
    }

    // DESTROY
    public void destroy() {
        player.stopTrack();
        playerManager.shutdown();
        player.destroy();
    }
}
