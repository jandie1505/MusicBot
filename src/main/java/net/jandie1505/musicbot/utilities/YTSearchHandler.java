package net.jandie1505.musicbot.utilities;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class YTSearchHandler {

    private YTSearchHandler() {}

    public static List<AudioTrack> search(String query) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        CountDownLatch latch = new CountDownLatch(1);
        List<AudioTrack> result = new ArrayList<>();

        AudioSourceManagers.registerRemoteSources(playerManager);

        playerManager.loadItem("ytsearch:" + query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                result.add(audioTrack);
                latch.countDown();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                result.addAll(audioPlaylist.getTracks());
                latch.countDown();
            }

            @Override
            public void noMatches() {
                latch.countDown();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                latch.countDown();
            }
        });

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // ignored, will return null
        }

        if (result.size() > 20) {

            int index = 0;
            for (AudioTrack track : List.copyOf(result)) {

                if (index > 20) {
                    result.remove(track);
                }

                index++;
            }

        }

        return result;
    }
}
