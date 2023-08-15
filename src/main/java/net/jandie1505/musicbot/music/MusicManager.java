package net.jandie1505.musicbot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.database.GuildData;

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

    // PLAYER

    public boolean isPaused(Guild g) {
        return getMusicPlayer(g.getIdLong()).isPaused();
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

            GuildData guildData = this.musicBot.getDatabaseManager().getGuild(guildId);

            if (guildData.getDefaultVolume() >= 0 && guildData.getDefaultVolume() <= 200 && guildData.getDefaultVolume() != 100) {
                musicPlayer.setVolume(guildData.getDefaultVolume());
            }

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

    public Map<Long, MusicPlayer> getPlayers() {
        return Map.copyOf(this.musicPlayers);
    }

    public MusicBot getMusicBot() {
        return this.musicBot;
    }
}
