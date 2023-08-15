package net.jandie1505.musicbot.eventlisteners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.database.GuildData;
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
            case "connect" -> connectCommand(interaction);
            case "mbsettings" -> mbsettingsCommand(interaction);
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

            if (list.length() > 512) {
                break;
            }

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

            if (source.startsWith("https://youtube.com/") || source.startsWith("https://www.youtube.com/")) {

                response = musicPlayer.enqueueWithResponse(interaction.getOption("song").getAsString(), startafterload);

            } else if (source.startsWith("https://open.spotify.com/")) {

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

            if (list.length() > 512) {
                break;
            }

            list = list + "`" + i + ".` `" + Messages.formatTime(track.getDuration()) + "` " + track.getInfo().title + " [" + track.getInfo().author + "]\n";
        }

        list = list + "`Found " + response.size() + " search results`";

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

    public void connectCommand(SlashCommandInteraction interaction) {

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

        // Get Channel

        AudioChannel channel = null;

        if (interaction.getOption("channel") != null) {

            if (interaction.getOption("channel").getAsChannel() instanceof AudioChannel) {
                channel = interaction.getOption("channel").getAsChannel().asAudioChannel();
            }

        } else {

            if (interaction.getMember().getVoiceState() != null && interaction.getMember().getVoiceState().getChannel() != null) {
                channel = interaction.getMember().getVoiceState().getChannel();
            }

        }

        if (channel == null) {
            interaction.getHook().sendMessageEmbeds(Messages.failMessage("Channel not valid or not found")).queue();
            return;
        }

        this.musicBot.getMusicManager().connect(channel);

        interaction.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setDescription(":white_check_mark:  Successfully connected")
                        .setColor(Color.GREEN)
                        .build()
        ).queue();

    }

    public void mbsettingsCommand(SlashCommandInteraction interaction) {

        // Null checks

        if (interaction.getGuild() == null || interaction.getMember() == null) {
            return;
        }

        // Permission check

        if (!this.musicBot.getGMS().memberHasAdminPermissions(interaction.getMember())) {
            return;
        }

        // Option

        if (interaction.getOption("action") == null) {
            return;
        }

        int action = interaction.getOption("action").getAsInt();

        // Defer Reply

        interaction.deferReply(true).queue();

        // Guild Data

        GuildData guildData = this.musicBot.getDatabaseManager().getGuild(interaction.getGuild().getIdLong());

        // Action

        switch (action) {
            case 0 -> {

                interaction.getHook().sendMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle("MusicBot Settings")
                                .addField("DJ Roles", this.getDJRoleList(interaction.getGuild()), false)
                                .addField("Restrict to Roles:", this.getRestrictToRolesStatus(interaction.getGuild()), false)
                                .addField("Ephemeral Messages:", guildData.isEphemeralState() ? "enabled" : "disabled", false)
                                .addField("Default Volume:", String.valueOf(guildData.getDefaultVolume()), false)
                                .setColor(Color.GRAY)
                                .build()
                ).queue();

            }
            case 1 -> {

                interaction.getHook().sendMessageEmbeds(
                        new EmbedBuilder()
                                .addField("DJ Roles:", this.getDJRoleList(interaction.getGuild()), false)
                                .setColor(Color.GRAY)
                                .build()
                ).queue();

            }
            case 2 -> {

                if (interaction.getOption("value") == null) {
                    interaction.getHook().sendMessageEmbeds(Messages.failMessage("You need to specify the role")).queue();
                    return;
                }

                Role role = interaction.getGuild().getRoleById(interaction.getOption("value").getAsString());

                if (role == null) {

                    for (Role otherRole : interaction.getGuild().getRolesByName(interaction.getOption("value").getAsString(), true)) {
                        role = otherRole;
                        break;
                    }

                }

                if (role == null) {
                    interaction.getHook().sendMessageEmbeds(Messages.warningMessage("Role does not exist")).queue();
                    return;
                }

                guildData.getDjRoles().add(role.getIdLong());
                this.musicBot.getDatabaseManager().updateGuild(guildData);

                interaction.getHook().sendMessageEmbeds(
                        new EmbedBuilder()
                                .setDescription(":white_check_mark:  Set role " + role.getName() + " (" + role.getIdLong() + ") as a DJ role")
                                .setColor(Color.GREEN)
                                .build()
                ).queue();

            }
            case 3 -> {

                if (interaction.getOption("value") == null) {
                    interaction.getHook().sendMessageEmbeds(Messages.failMessage("You need to specify the role")).queue();
                    return;
                }

                try {
                    guildData.getDjRoles().remove(Long.parseLong(interaction.getOption("value").getAsString()));
                    this.musicBot.getDatabaseManager().updateGuild(guildData);
                } catch (IllegalArgumentException e) {
                    interaction.getHook().sendMessageEmbeds(Messages.warningMessage("You need to specify a valid **role id**")).queue();
                    return;
                }

                interaction.getHook().sendMessageEmbeds(
                        new EmbedBuilder()
                                .setDescription(":negative_squared_cross_mark:  Role " + Long.parseLong(interaction.getOption("value").getAsString()) + " is no longer a DJ role")
                                .setColor(Color.GREEN)
                                .build()
                ).queue();

            }
            case 4 -> {

                guildData.getDjRoles().clear();
                this.musicBot.getDatabaseManager().updateGuild(guildData);

                interaction.getHook().sendMessageEmbeds(
                        new EmbedBuilder()
                                .setDescription(":wastebasket:  Cleared all DJ roles")
                                .setColor(Color.GREEN)
                                .build()
                ).queue();

            }
            case 5 -> {

                if (interaction.getOption("value") != null) {

                    try {
                        int status = Integer.parseInt(interaction.getOption("value").getAsString());

                        if (status < 0 || status > 2) {
                            interaction.getHook().sendMessageEmbeds(Messages.warningMessage("You need to set a value between 0 and 2. Run the command without a value for help.")).queue();
                            return;
                        }

                        guildData.setRestrictToRoles(status);
                        this.musicBot.getDatabaseManager().updateGuild(guildData);

                        interaction.getHook().sendMessageEmbeds(
                                new EmbedBuilder()
                                        .setDescription(":white_check_mark:  Restrict to roles status has been set")
                                        .setColor(Color.GREEN)
                                        .build()
                        ).queue();

                    } catch (IllegalArgumentException e) {
                        interaction.getHook().sendMessageEmbeds(Messages.warningMessage("You need to specify a valid id")).queue();
                    }

                } else {

                    interaction.getHook().sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Restrict to Roles")
                                    .addField("Current status:", this.getRestrictToRolesStatus(interaction.getGuild()), false)
                                    .addField("How to set:", "Specify the id of the status as the value.", false)
                                    .addField("Available statuses:",
                                            "`0` - :lock:  Admin/DJ only mode (Only Admins/DJs can use the bot)\n" +
                                            "`1` - :unlock:  Normal mode (Normal users can add songs and skipvote)\n" +
                                            "`2` - :infinity:  Unlimited mode (Normal users have DJ permissions)",
                                            false
                                    )
                                    .setColor(Color.GRAY)
                                    .build()
                    ).queue();

                }

            }
            case 6 -> {

                if (interaction.getOption("value") != null) {

                    String value = interaction.getOption("value").getAsString();

                    switch (value) {
                        case "enable" -> guildData.setEphemeralState(true);
                        case "disable" -> guildData.setEphemeralState(false);
                        case "true", "false" -> guildData.setEphemeralState(Boolean.parseBoolean(value));
                        default -> {
                            interaction.getHook().sendMessageEmbeds(Messages.warningMessage("You need to specify a valid value. Run the command without value for help.")).queue();
                            return;
                        }
                    }

                    this.musicBot.getDatabaseManager().updateGuild(guildData);

                    interaction.getHook().sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Ephemeral messages status set")
                                    .setColor(Color.GREEN)
                                    .build()
                    ).queue();

                } else {

                    interaction.getHook().sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Ephemeral Messages:")
                                    .addField("Current Status:", guildData.isEphemeralState() ? "enabled" : "disabled", false)
                                    .addField("How to set:", "Set enable/true or disable/false as the value.", false)
                                    .addField("What this is:", "Ephemeral messages are special command replys that only the user that has run the command can see.\nEnabling this is recommended.", false)
                                    .build()
                    ).queue();

                }

            }
            case 7 -> {

                if (interaction.getOption("value") != null) {

                    try {
                        int status = Integer.parseInt(interaction.getOption("value").getAsString());

                        if (status < 0 || status > 200) {
                            interaction.getHook().sendMessageEmbeds(Messages.warningMessage("You need to set a value between 0 and 200. Run the command without a value for help.")).queue();
                            return;
                        }

                        guildData.setDefaultVolume(status);
                        this.musicBot.getDatabaseManager().updateGuild(guildData);

                        interaction.getHook().sendMessageEmbeds(
                                new EmbedBuilder()
                                        .setDescription(":white_check_mark:  Default volume has been set")
                                        .setColor(Color.GREEN)
                                        .build()
                        ).queue();

                    } catch (IllegalArgumentException e) {
                        interaction.getHook().sendMessageEmbeds(Messages.warningMessage("You need to specify a valid volume value")).queue();
                    }

                } else {

                    interaction.getHook().sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Restrict to Roles")
                                    .addField("Current volume:", String.valueOf(guildData.getDefaultVolume()), false)
                                    .addField("How to set:", "Set the volume value between 0 and 200 as the value.", false)
                                    .setColor(Color.GRAY)
                                    .build()
                    ).queue();

                }

            }
            default -> interaction.getHook().sendMessageEmbeds(Messages.warningMessage("Unknown action")).queue();
        }

    }

    private String getDJRoleList(Guild guild) {
        GuildData guildData = this.musicBot.getDatabaseManager().getGuild(guild.getIdLong());
        String djRoles = "";

        for (long roleId : guildData.getDjRoles()) {
            Role role = guild.getRoleById(roleId);

            if (role == null) {
                djRoles = djRoles + "`" + roleId + "`,\n";
                continue;
            }

            djRoles = djRoles + role.getName() + " (`" + roleId + "`),\n";
        }

        djRoles = djRoles + "The server has " + guildData.getDjRoles().size() + " DJ roles";

        return djRoles;
    }

    private String getRestrictToRolesStatus(Guild guild) {
        GuildData guildData = this.musicBot.getDatabaseManager().getGuild(guild.getIdLong());
        String restrictToRoles = ":question:  Unknown status";

        switch (guildData.getRestrictToRoles()) {
            case 0 -> restrictToRoles = ":lock:  Admin/DJ only mode (0)";
            case 1 -> restrictToRoles = ":unlock:  Normal mode";
            case 2 -> restrictToRoles = ":infinity:  Unlimited mode";
        }

        return restrictToRoles;
    }

}
