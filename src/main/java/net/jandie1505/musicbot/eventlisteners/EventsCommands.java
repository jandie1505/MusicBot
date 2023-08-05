package net.jandie1505.musicbot.eventlisteners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.music.MusicEqueuedResponse;
import net.jandie1505.musicbot.music.MusicPlayer;
import net.jandie1505.musicbot.utilities.Messages;
import net.jandie1505.musicbot.utilities.SpotifySearchHandler;
import net.jandie1505.musicbot.utilities.YTSearchHandler;

import java.awt.*;
import java.util.List;

public class EventsCommands extends ListenerAdapter {
    private final MusicBot musicBot;

    public EventsCommands(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        SlashCommandInteraction interaction = event.getInteraction();

        switch (interaction.getName()) {
            case "nowplaying" -> nowplayingCommand(interaction);
            case "queue" -> queueCommand(interaction);
            case "play" -> playCommand(interaction);
            case "stop" -> stopCommand(interaction);
            case "disconnect" -> disconnectCommand(interaction);
            default -> {}
        }

    }

    private void nowplayingCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasUserPermissions(interaction.getMember())) {
            return;
        }

        // Defer reply

        interaction.deferReply(this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong()).isEphemeralState()).queue();

        // Send message

        interaction.getHook().sendMessage(Messages.nowplayingMessage(this.musicBot, interaction.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(interaction.getMember())).build()).queue();

    }

    private void queueCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasUserPermissions(interaction.getMember())) {
            return;
        }

        // Defer reply

        interaction.deferReply(this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong()).isEphemeralState()).queue();

        // Get music player

        MusicPlayer musicPlayer = this.musicBot.getMusicManager().getMusicPlayer(interaction.getGuild().getIdLong());

        // Check if queue is empty

        if (musicPlayer.getQueue().isEmpty()) {

            interaction.getHook().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setDescription("The queue is empty")
                            .setColor(Color.GRAY)
                            .build()
            ).queue();

            return;
        }

        // List queue

        int startIndex = 0;

        if (interaction.getOption("index") != null) {
            startIndex = interaction.getOption("index").getAsInt();
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        String list = "";

        int max = musicPlayer.getQueue().size();

        if ((max - startIndex) > 10) {
            max = startIndex + 10;
        }

        for (int i = startIndex; i < max; i++) {
            AudioTrack track = musicPlayer.getQueue().get(i);

            list = list + "`" + i + ".` `" + Messages.formatTime(track.getDuration()) + "` " + track.getInfo().title + " [" + track.getInfo().author + "]\n";

        }

        list = list + "`Showing track " + startIndex + "-" + max + " with a total of " + musicPlayer.getQueue().size() + " tracks.`\n";
        list = list + "`Use /queue [index] to start the list from a specific index.`";

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .addField("Queue:", list, false)
                        .setColor(Color.GRAY)
                        .build()
        ).queue();

    }

    private void playCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasUserPermissions(interaction.getMember())) {
            return;
        }

        // Defer reply

        interaction.deferReply(this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong()).isEphemeralState()).queue();

        // Connect

        if (this.musicBot.getMusicManager().isConnected(interaction.getGuild())) {

            if (!this.musicBot.getGMS().memberHasDJPermissions(interaction.getMember()) && interaction.getMember().getVoiceState().getChannel() != interaction.getGuild().getSelfMember().getVoiceState().getChannel()) {

                interaction.getHook().sendMessageEmbeds(
                        Messages.warningMessage("You need to be in the same channel as the bot")
                ).queue();

                return;
            }

        } else {

            if (!interaction.getMember().getVoiceState().inAudioChannel()) {

                interaction.getHook().sendMessageEmbeds(
                        Messages.warningMessage("You are not in a voice channel")
                ).queue();

                return;
            }

            if (!this.musicBot.getMusicManager().connect(interaction.getMember().getVoiceState().getChannel())) {

                interaction.getHook().sendMessageEmbeds(
                        Messages.warningMessage("Can't connect to voice channel (Missing permissions?)")
                ).queue();

                return;
            }

        }

        // Get Music Player

        MusicPlayer musicPlayer = this.musicBot.getMusicManager().getMusicPlayer(interaction.getGuild().getIdLong());

        // Null check

        if (interaction.getOption("song") == null || interaction.getOption("song").getAsString() == null) {
            return;
        }

        // Enqueue

        boolean startafterload = musicPlayer.getPlayingTrack() == null;
        String source = interaction.getOption("name").getAsString();
        MusicEqueuedResponse response = null;

        if (source.startsWith("http://") || source.startsWith("https://")) {

            if (source.startsWith("https://youtube.com/")) {

                response = musicPlayer.enqueueWithResponse(interaction.getOption("song").getAsString(), startafterload);


            } else if (source.startsWith("https://spotify.com/")) {

                List<AudioTrack> tracks = SpotifySearchHandler.search(source, this.musicBot.getConfigManager().getConfig().getSpotifyClientId(), this.musicBot.getConfigManager().getConfig().getSpotifyClientSecret());

                for (AudioTrack track : tracks) {
                    musicPlayer.enqueue(source, startafterload);
                }

                if (!tracks.isEmpty()) {
                    response = new MusicEqueuedResponse(tracks.size());
                }

            }

        } else {

            List<AudioTrack> tracks = YTSearchHandler.search(source);

            if (!tracks.isEmpty()) {
                response = musicPlayer.enqueueWithResponse(tracks.get(0).getInfo().uri, startafterload);
            }

        }

        // Handle result

        if (response == null) {

            interaction.getHook().sendMessageEmbeds(
                    Messages.failMessage("Error while loading audio track(s)")
            ).queue();

            return;
        }

        if (response.isPlaylist()) {

            interaction.getHook().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setDescription(":white_check_mark:  Added playlist with " + response.getLength() + " tracks to queue")
                            .setColor(Color.GREEN)
                            .build()
            ).queue();

        } else {

            interaction.getHook().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setDescription(":white_check_mark:  Added track " + response.getTitle() + " to queue")
                            .setColor(Color.GREEN)
                            .build()
            ).queue();

        }

    }

    private void stopCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasDJPermissions(interaction.getMember())) {
            return;
        }

        // Defer reply

        interaction.deferReply(this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong()).isEphemeralState()).queue();

        // Get music player

        MusicPlayer musicPlayer = this.musicBot.getMusicManager().getMusicPlayer(interaction.getGuild().getIdLong());

        // check if paused

        if (musicPlayer.isPaused()) {

            musicPlayer.clearQueue();
            musicPlayer.stop();

            interaction.getHook().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setDescription(":stop_button:  Player stopped and queue cleared")
                            .setColor(Color.GREEN)
                            .build()
            ).queue();

        } else {

            musicPlayer.setPause(true);

            interaction.getHook().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setDescription(":pause_button:  Player paused")
                            .setColor(Color.GREEN)
                            .build()
            ).queue();

        }

    }

    private void disconnectCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasDJPermissions(interaction.getMember())) {
            return;
        }

        // Defer reply

        interaction.deferReply(this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong()).isEphemeralState()).queue();

        // Disconnect bot

        this.musicBot.getMusicManager().disconnect(interaction.getGuild());

        // reply

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(":heavy_multiplication_x:  Disconnected")
                        .build()
        ).queue();

    }
}
