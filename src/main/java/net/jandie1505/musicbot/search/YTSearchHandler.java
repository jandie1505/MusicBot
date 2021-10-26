package net.jandie1505.musicbot.search;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YTSearchHandler {
    private boolean await;
    private List<AudioTrack> result;
    private AudioPlayerManager manager;

    private YTSearchHandler() {
        await = false;
        result = new ArrayList<>();
        manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(manager);
    }

    private List<AudioTrack> awaitSearch(String query) {
        this.searchHandler(query);
        while(await) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        manager.shutdown();
        return result;
    }

    private void searchHandler(String query) {
        String searchQuery = "ytsearch:" + query;
        this.startAwait();
        manager.loadItem(searchQuery, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                result.add(track);
                stopAwait();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                result.addAll(playlist.getTracks());
                stopAwait();
            }

            @Override
            public void noMatches() {
                stopAwait();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                stopAwait();
            }
        });
    }

    private void startAwait() {
        await = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                await = false;
            }
        }).start();
    }

    private void stopAwait() {
        await = false;
    }



    public static List<AudioTrack> search(String query) {
        YTSearchHandler searchHandler = new YTSearchHandler();
        return searchHandler.awaitSearch(query);
    }
}
