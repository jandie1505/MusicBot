package net.jandie1505.musicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.music.MusicPlayer;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Messages {
    public static MessageCreateBuilder nowplayingMessage(MusicBot musicBot, Guild g, boolean showbuttons) {

        MusicPlayer player = musicBot.getMusicManager().getMusicPlayer(g.getIdLong());

        if(player.getPlayingTrack() != null) {
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
            AudioTrack audioTrack = player.getPlayingTrack();
            String description = "";
            if(player.isPaused()) {
                description = ":pause_button:  Player is currently paused";
            } else {
                description = ":arrow_forward:  Player is currently playing";
            }
            String playIcon = ":stop_button:";
            String progressbar = "▬▬▬▬▬▬▬▬▬▬";

            if(player.isPaused()) {
                playIcon = ":pause_button:";
                progressbar = getProgressBar(audioTrack.getPosition(), audioTrack.getDuration());
            } else if(!player.isPaused()) {
                playIcon = ":arrow_forward:";
                progressbar = getProgressBar(audioTrack.getPosition(), audioTrack.getDuration());
            }

            if(showbuttons) {
                if(player.isPaused()) {
                    messageBuilder.addComponents(ActionRow.of(
                            Button.primary("nowplaying_button_play", "▶"),
                            Button.primary("nowplaying_button_skip", "⏭"),
                            Button.secondary("nowplaying_button_refresh", "\uD83D\uDD04")
                    ));
                } else if(!player.isPaused()) {
                    messageBuilder.addComponents(ActionRow.of(
                            Button.primary("nowplaying_button_pause", "⏸"),
                            Button.primary("nowplaying_button_skip", "⏭"),
                            Button.secondary("nowplaying_button_refresh", "\uD83D\uDD04")
                    ));
                }
            }

            String channelString = "";
            if(g.getSelfMember().getVoiceState().inAudioChannel()) {
                channelString = g.getSelfMember().getVoiceState().getChannel().getName();
            } else {
                channelString = "---";
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(audioTrack.getInfo().title, audioTrack.getInfo().uri)
                    .setDescription(playIcon + " " + progressbar + " `" + formatTime(audioTrack.getPosition()) + "/" + formatTime(audioTrack.getDuration()) + "` \nAuthor: " + audioTrack.getInfo().author + "\nChannel: " + channelString + "\nVolume: " + player.getVolume());
            messageBuilder.setEmbeds(embedBuilder.build());

            return messageBuilder;
        } else {
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
            String channelString = "";
            if(g.getSelfMember().getVoiceState().inAudioChannel()) {
                channelString = g.getSelfMember().getVoiceState().getChannel().getName();
            } else {
                channelString = "---";
            }
            EmbedBuilder noMusicPlaying = new EmbedBuilder()
                    .setTitle("No music playing")
                    .setDescription(":stop_button: ▬▬▬▬▬▬▬▬▬▬ `--:--:--/--:--:--` \nAuthor: ---\nChannel: " + channelString + "\nVolume: " + player.getVolume());
            messageBuilder.setEmbeds(noMusicPlaying.build());

            messageBuilder.setComponents(ActionRow.of(
                    Button.secondary("nowplaying_button_refresh", "\uD83D\uDD04")
            ));

            return messageBuilder;
        }

    }

    public static MessageCreateBuilder getHelpMessage() {
        return new MessageCreateBuilder()
                .setEmbeds(
                        new EmbedBuilder()
                                .setTitle("MusicBot Help")
                                .setDescription("MusicBot by jandie1505")
                                .addField("For users:", "/play <song name / link> - Add a specific song to queue\n" +
                                        "/skip - Vote to skip a specific song\n" +
                                        "/nowplaying - Get the song that is currently playing\n" +
                                        "/queue - Show the queue\n" +
                                        "/queue <index> - Show the queue from a certain index (\"Queue pages\")\n" +
                                        "/search <song name> - Search for a specific song and list the result", false)
                                .addField("For DJs:", "/pause - Pause the player\n" +
                                        "/resume - Resume the player\n" +
                                        "/stop - Stops the player (only when paused, else pauses the player)\n" +
                                        "/connect [channel] - Connect to a channel\n" +
                                        "/disconnect - Disconnect the bot\n" +
                                        "/forceskip - Skip a track\n" +
                                        "/movetrack <from> <to> - Move a specific track in queue\n" +
                                        "/remove <index> - Remove a specific song from queue\n" +
                                        "/clear - Clear the queue\n" +
                                        "/shuffle - Randomizes the order of the queue\n" +
                                        "/volume <value> - Change the volume (0-200)\n" +
                                        "/playnow <song name / link> - Plays a specific song immediately", false)
                                .addField("For Admins:", "/mbsettings - Change settings of the bot", false)
                                .build()
                );
    }

    // UTILITY
    public static String formatTime(long millis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static String getProgressBar(double position, double duration) {
        String progressbar = "▬▬▬▬▬▬▬▬▬▬";
        if(duration > 0) {
            double percent = (position / duration) * 100;
            if(percent >= 90) {
                progressbar = "▬▬▬▬▬▬▬▬▬:radio_button:";
            } else if(percent >= 80) {
                progressbar = "▬▬▬▬▬▬▬▬:radio_button:▬";
            } else if(percent >= 70) {
                progressbar = "▬▬▬▬▬▬▬:radio_button:▬▬";
            } else if(percent >= 60) {
                progressbar = "▬▬▬▬▬▬:radio_button:▬▬▬";
            } else if(percent >= 50) {
                progressbar = "▬▬▬▬▬:radio_button:▬▬▬▬";
            } else if(percent >= 40) {
                progressbar = "▬▬▬▬:radio_button:▬▬▬▬▬";
            } else if(percent >= 30) {
                progressbar = "▬▬▬:radio_button:▬▬▬▬▬▬";
            } else if(percent >= 20) {
                progressbar = "▬▬:radio_button:▬▬▬▬▬▬▬";
            } else if(percent >= 10) {
                progressbar = "▬:radio_button:▬▬▬▬▬▬▬▬";
            } else if(percent >= 0) {
                progressbar = ":radio_button:▬▬▬▬▬▬▬▬▬";
            } else {
                progressbar = "▬▬▬▬▬▬▬▬▬▬";
            }
        }
        return progressbar;
    }

    public static MessageEmbed warningMessage(String message) {
        return new EmbedBuilder()
                .setDescription(":warning:  " + message)
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed failMessage(String message) {
        return new EmbedBuilder()
                .setDescription(":x:  " + message)
                .setColor(Color.RED)
                .build();
    }
}
