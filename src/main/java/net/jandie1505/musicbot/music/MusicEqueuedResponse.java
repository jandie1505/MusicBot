package net.jandie1505.musicbot.music;

public class MusicEqueuedResponse {
    private final String title;
    private final boolean playlist;
    private final int length;

    public MusicEqueuedResponse(String title) {
        this.title = title;
        this.playlist = false;
        this.length = 1;
    }

    public MusicEqueuedResponse(int length) {
        this.title = "";
        this.playlist = true;
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPlaylist() {
        return playlist;
    }

    public int getLength() {
        return length;
    }
}
