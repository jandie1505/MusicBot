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
import java.util.Random;

public class EventsCommands extends ListenerAdapter {
    private final MusicBot musicBot;

    public EventsCommands(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        SlashCommandInteraction interaction = event.getInteraction();

        switch (interaction.getName()) {
            case "help" -> helpCommand(interaction);
            case "cmd" -> cmdCommand(interaction);
            case "nowplaying" -> nowplayingCommand(interaction);
            case "queue" -> queueCommand(interaction);
            case "play" -> playCommand(interaction);
            case "stop" -> stopCommand(interaction);
            case "disconnect" -> disconnectCommand(interaction);
            case "forceskip" -> forceskipCommand(interaction);
            case "remove" -> removeCommand(interaction);
            case "pause" -> pauseCommand(interaction);
            case "resume" -> resumeCommand(interaction);
            case "clear" -> clearCommand(interaction);
            case "playnow" -> playnowCommand(interaction);
            case "movetrack" -> movetrackCommand(interaction);
            case "shuffle" -> shuffleCommand(interaction);
            case "search" -> searchCommand(interaction);
            case "volume" -> volumeCommand(interaction);
            default -> {}
        }

    }

    private void helpCommand(SlashCommandInteraction interaction) {

        interaction.deferReply(true).queue();

        interaction.getHook().sendMessage(
                Messages.getHelpMessage().build()
        ).queue();

    }

    private void cmdCommand(SlashCommandInteraction interaction) {

        if (this.musicBot.getConfig().optLong("botOwner", -1) < 0) {
            return;
        }

        if (this.musicBot.getConfig().optLong("botOwner", -1) != interaction.getUser().getIdLong()) {
            return;
        }

        if (interaction.getOption("cmd") == null) {
            return;
        }

        interaction.deferReply(true).queue();

        String command = interaction.getOption("cmd").getAsString();
        String response = this.musicBot.getConsole().runCommand(command);

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .addField("Executed command:", command, false)
                        .addField("Response:", response, false)
                        .setColor(Color.BLACK)
                        .build()
        ).queue();

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
        String source = interaction.getOption("song").getAsString();
        MusicEqueuedResponse response = null;

        if (source.startsWith("http://") || source.startsWith("https://")) {

            if (source.startsWith("https://youtube.com/")) {

                response = musicPlayer.enqueueWithResponse(interaction.getOption("song").getAsString(), startafterload);


            } else if (source.startsWith("https://spotify.com/")) {

                List<AudioTrack> tracks = SpotifySearchHandler.search(source, this.musicBot.getConfig().optString("spotifyClientId", ""), this.musicBot.getConfig().optString("spotifyClientSecret", ""));

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
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    private void forceskipCommand(SlashCommandInteraction interaction) {

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

        if (musicPlayer.getPlayingTrack() == null && musicPlayer.getQueue().isEmpty()) {

            interaction.getHook().sendMessageEmbeds(
                    Messages.failMessage("Nothing to skip")
            ).queue();

            return;
        }

        musicPlayer.nextTrack();

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription("Skipped track")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    private void removeCommand(SlashCommandInteraction interaction) {

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

        // get option

        if (interaction.getOption("index") == null) {
            return;
        }

        int index = interaction.getOption("index").getAsInt();

        // Get music player

        MusicPlayer musicPlayer = this.musicBot.getMusicManager().getMusicPlayer(interaction.getGuild().getIdLong());

        // Check if index exists

        if (index >= musicPlayer.getQueue().size() || index < 0) {

            interaction.getHook().sendMessageEmbeds(
                    Messages.failMessage("Index does not exist")
            ).queue();

            return;
        }

        // Remove track

        musicPlayer.removeTrack(index);

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(":wastebasket:  Track removed from queue")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    private void pauseCommand(SlashCommandInteraction interaction) {

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

        // options

        if (interaction.getOption("state") != null) {

            musicPlayer.setPause(interaction.getOption("state").getAsBoolean());

            interaction.getHook().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setDescription("Pause set to: " + interaction.getOption("state").getAsBoolean())
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

    private void resumeCommand(SlashCommandInteraction interaction) {

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

        // resume

        if (musicPlayer.isPaused()) {
            musicPlayer.setPause(false);
        }

        if (musicPlayer.getPlayingTrack() == null) {
            musicPlayer.nextTrack();
        }

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(":arrow_forward:  Player resumed")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    private void clearCommand(SlashCommandInteraction interaction) {

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

        // Clear queue

        musicPlayer.clearQueue();

        // reply

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(":wastebasket::asterisk:  Queue cleared")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    private void playnowCommand(SlashCommandInteraction interaction) {

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

        // Option

        if (interaction.getOption("song") == null || interaction.getOption("song").getAsString() == null) {
            return;
        }

        String source = interaction.getOption("song").getAsString();

        // Get music player

        MusicPlayer musicPlayer = this.musicBot.getMusicManager().getMusicPlayer(interaction.getGuild().getIdLong());

        // play

        MusicEqueuedResponse response = null;

        if (source.startsWith("https://youtube.com/")) {

            response = musicPlayer.playnowWithResponse(source);

        } else {

            List<AudioTrack> tracks = YTSearchHandler.search(source);

            if (!tracks.isEmpty()) {
                response = musicPlayer.playnowWithResponse(tracks.get(0).getInfo().uri);
            }

        }

        // reply

        if (response == null) {

            interaction.getHook().sendMessageEmbeds(
                    Messages.failMessage("Nothing found")
            ).queue();

            return;
        }

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(":arrow_forward:  Playing track " + response.getTitle() + " now")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    private void movetrackCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasDJPermissions(interaction.getMember())) {
            return;
        }

        // Option

        if (interaction.getOption("from") == null || interaction.getOption("to") == null) {
            return;
        }

        // Defer reply

        interaction.deferReply(this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong()).isEphemeralState()).queue();

        // Get music player

        MusicPlayer musicPlayer = this.musicBot.getMusicManager().getMusicPlayer(interaction.getGuild().getIdLong());

        // move track

        musicPlayer.moveTrack(interaction.getOption("from").getAsInt(), interaction.getOption("to").getAsInt());

        // reply

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(":ballot_box_with_check:  Track moved")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    private void shuffleCommand(SlashCommandInteraction interaction) {

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

        // Shuffle queue

        musicPlayer.shuffle();

        // Reply

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(":twisted_rightwards_arrows:  Queue shuffled")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    public void searchCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasDJPermissions(interaction.getMember())) {
            return;
        }

        // Option

        if (interaction.getOption("query") == null) {
            return;
        }

        // Defer reply

        interaction.deferReply(this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong()).isEphemeralState()).queue();

        // Search

        List<AudioTrack> response = YTSearchHandler.search(interaction.getOption("query").getAsString());

        // Empty search

        if (response.isEmpty()) {
            interaction.getHook().sendMessageEmbeds(Messages.failMessage("No search results")).queue();
            return;
        }

        // List

        String list = "";

        for (int i = 0; i < response.size(); i++) {
            AudioTrack track = response.get(i);

            list = list + "`" + i + ".` `" + Messages.formatTime(track.getDuration()) + "` " + track.getInfo().title + " [" + track.getInfo().author + "]\n";
        }

        list = list + "`Showing " + response.size() + " search results`";

        // Reply

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .addField("Search results:", list, false)
                        .build()
        ).queue();

    }

    public void volumeCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasDJPermissions(interaction.getMember())) {
            return;
        }

        // OS architecture check

        if (!System.getProperty("os.arch").equalsIgnoreCase("amd64")) {
            interaction.replyEmbeds(Messages.failMessage("Unsupported platform")).queue();
            return;
        }

        // Option

        if (interaction.getOption("volume") == null) {
            return;
        }

        int volume = interaction.getOption("volume").getAsInt();

        // Defer reply

        interaction.deferReply(this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong()).isEphemeralState()).queue();

        // Get music player

        MusicPlayer musicPlayer = this.musicBot.getMusicManager().getMusicPlayer(interaction.getGuild().getIdLong());

        // check volume

        if (volume < 0) {
            interaction.getHook().sendMessageEmbeds(Messages.warningMessage("Active noise cancelling is not supported")).queue();
            return;
        }
        
        if (volume > 200) {
            
            if (new Random().nextInt(10) < 2) {
                interaction.getHook().sendMessage("The volume has been capped at 200 to avoid this:\nhttps://tenor.com/view/explosion-mushroom-cloud-atomic-bomb-bomb-boom-gif-4464831").queue();
            } else {

                interaction.getHook().sendMessageEmbeds(
                        new EmbedBuilder()
                                .setDescription("The volume has been capped at 200 so you don't end up like this guy: :exploding_head:")
                                .setColor(Color.RED)
                                .build()
                ).queue();
                
            }
            
            return;
        }

        // set volume

        musicPlayer.setVolume(volume);
        
        // Reply

        String replyString = "";
        
        if (volume == 200) {

            if (new Random().nextInt(10) < 2) {

                interaction.getHook().sendMessage(
                        "Volume set to 200\n" +
                           "https://tenor.com/view/nuclear-catastrophic-disastrous-melt-down-gif-13918708"
                ).queue();

                return;
            } else {
                replyString = ":exploding_head:  Volume set to 200";
            }

        } else if (volume >= 150) {
            replyString = ":loud_sound::boom:  Volume set to " + volume;
        } else if (volume >= 100) {
            replyString = ":loud_sound:  Volume set to " + volume;
        } else if (volume >= 50) {
            replyString = ":sound:  Volume set to " + volume;
        } else if (volume == 0) {
            replyString = ":mute:  Volume muted";
        } else {
            replyString = ":speaker:  Volume set to " + volume;
        }

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(replyString)
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

}
