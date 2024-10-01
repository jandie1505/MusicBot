package net.jandie1505.musicbot.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.database.GuildData;
import net.jandie1505.musicbot.utilities.BotStatus;

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
            audioChannel.getGuild().getAudioManager().setSelfDeafened(true);

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

    public void reloadChannelConnections() {

        if (this.musicBot.getBotStatus() != BotStatus.ACTIVE) {
            return;
        }

        for (Guild g : List.copyOf(this.musicBot.getShardManager().getGuilds())) {

            if (g.getSelfMember().getVoiceState() == null) {
                continue;
            }

            if (!g.getSelfMember().getVoiceState().inAudioChannel()) {
                continue;
            }

            if (g.getSelfMember().getVoiceState().getChannel() == null) {
                continue;
            }

            List<Member> members = new ArrayList<>(g.getSelfMember().getVoiceState().getChannel().getMembers());

            members.removeIf(m -> m.getIdLong() == g.getSelfMember().getIdLong());

            if (!members.isEmpty()) {
                continue;
            }

            this.disconnect(g);

        }

    }

    // PLAYER MANAGEMENT

    public void reloadPlayers() {

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

            if (g.getSelfMember().getVoiceState() == null) {
                this.removePlayer(guildId);
                continue;
            }

            if (!g.getSelfMember().getVoiceState().inAudioChannel()) {
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
