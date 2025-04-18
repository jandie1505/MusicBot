package net.jandie1505.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.jandie1505.musicbot.MusicBot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MusicPlayer {
    private final MusicManager musicManager;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final AudioPlayerSendHandler playerSendHandler;
    private final TrackScheduler trackScheduler;
    private final List<AudioTrack> queue;

    public MusicPlayer(MusicManager musicManager) {
        this.musicManager = musicManager;
        this.playerManager = new DefaultAudioPlayerManager();
        this.player = playerManager.createPlayer();
        this.playerSendHandler = new AudioPlayerSendHandler(this.player);
        this.trackScheduler = new TrackScheduler(this);
        this.queue = Collections.synchronizedList(new ArrayList<>());

        YoutubeAudioSourceManager source = new YoutubeAudioSourceManager(
                new Music(),
                new Web(),
                new MWeb(),
                new WebEmbedded(),
                new Android(),
                new AndroidMusic(),
                new AndroidVr(),
                new Ios(),
                new Tv(),
                new TvHtml5Embedded()
        );
        String oauthToken = this.musicManager.getMusicBot().getConfig().optString("youTubeSourceOAuthToken");
        if (oauthToken != null && !oauthToken.isEmpty()) source.useOauth2(oauthToken, true);
        this.playerManager.registerSourceManager(source);
        this.player.addListener(this.trackScheduler);
    }

    // QUEUE

    private void load(String source, AudioLoadResult result) {
        this.playerManager.loadItem(source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                result.runAfterLoad(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                result.runAfterLoad(playlist);
            }

            @Override
            public void noMatches() {
                result.runAfterLoad(null);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                MusicBot.LOGGER.error("FriendlyException while loading track", e);
                result.runAfterLoad(null);
            }
        });

    }

    public void enqueue(String source, @Nullable TrackInfoResult info) {

        this.load(source, item -> {

            if (item instanceof AudioTrack track) {
                MusicPlayer.this.queue.add(track);
                if (info != null) info.trackInfo(List.of(track.getInfo()));
            } else if (item instanceof AudioPlaylist playlist) {
                MusicPlayer.this.queue.addAll(playlist.getTracks());
                if (info != null) info.trackInfo(playlist.getTracks().stream().map(AudioTrack::getInfo).toList());
            } else {
                if (info != null) info.trackInfo(null);
            }

        });

    }

    public void enqueue(String source, boolean startAfterLoad) {
        this.enqueue(source, info -> {
            if (startAfterLoad) MusicPlayer.this.nextTrack();
        });
    }

    @Deprecated(forRemoval = true)
    public MusicEqueuedResponse enqueueWithResponse(String source, boolean startAfterLoad) {
        CountDownLatch latch = new CountDownLatch(1);
        final MusicEqueuedResponse[] response = {null};

        this.enqueue(source, item -> {

            if (item != null && !item.isEmpty()) {

                if (item.size() == 1) {
                    response[0] = new MusicEqueuedResponse(item.get(0).title);
                } else {
                    response[0] = new MusicEqueuedResponse(item.size());
                }

                if (startAfterLoad) MusicPlayer.this.nextTrack();

            }

            latch.countDown();

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

        if (volume < 0) {
            return;
        }

        if (!System.getProperty("os.arch").equalsIgnoreCase("amd64")) {
            return;
        }

        player.setVolume(volume);
    }

    public int getVolume() {
        return player.getVolume();
    }

    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    public void playnow(String source) {

        this.load(source, item -> {

            if (item instanceof AudioTrack track) {
                MusicPlayer.this.play(track);
            }

        });

    }

    @Deprecated(forRemoval = true)
    public MusicEqueuedResponse playnowWithResponse(String source) {
        CountDownLatch latch = new CountDownLatch(1);
        final MusicEqueuedResponse[] response = {null};

        this.load(source, item -> {

            if (item instanceof AudioTrack track) {
                MusicPlayer.this.play(track);
                response[0] = new MusicEqueuedResponse(track.getInfo().title);
            }

            latch.countDown();

        });

        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // ignored
        }

        return response[0];
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

    // ----- INNER CLASSES -----

    public interface AudioLoadResult {
        void runAfterLoad(@Nullable AudioItem item);
    }

    public interface TrackInfoResult {
        void trackInfo(@Nullable List<AudioTrackInfo> info);
    }
}
