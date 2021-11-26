package net.jandie1505.musicbot.system;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.jandie1505.musicbot.MusicBot;

import java.util.concurrent.TimeUnit;

public class Messages {
    public static MessageBuilder nowplayingMessage(Guild g, boolean showbuttons) {
        if(MusicManager.getPlayingTrack(g) != null) {
            MessageBuilder messageBuilder = new MessageBuilder();
            AudioTrack audioTrack = MusicManager.getPlayingTrack(g);
            String description = "";
            if(MusicManager.isPaused(g)) {
                description = ":pause_button:  Player is currently paused";
            } else {
                description = ":arrow_forward:  Player is currently playing";
            }
            String playIcon = ":stop_button:";
            String progressbar = "▬▬▬▬▬▬▬▬▬▬";

            if(MusicManager.isPaused(g)) {
                playIcon = ":pause_button:";
                progressbar = getProgressBar(audioTrack.getPosition(), audioTrack.getDuration());
            } else if(!MusicManager.isPaused(g)) {
                playIcon = ":arrow_forward:";
                progressbar = getProgressBar(audioTrack.getPosition(), audioTrack.getDuration());
            }

            if(showbuttons) {
                if(MusicManager.isPaused(g)) {
                    messageBuilder.setActionRows(ActionRow.of(Button.primary("playbutton", "▶"),
                            Button.primary("refreshbutton", "\uD83D\uDD04")));
                } else if(!MusicManager.isPaused(g)) {
                    messageBuilder.setActionRows(ActionRow.of(Button.primary("pausebutton", "⏸"),
                            Button.primary("refreshbutton", "\uD83D\uDD04")));
                }
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(audioTrack.getInfo().title, audioTrack.getInfo().uri)
                    .setDescription(playIcon + " " + progressbar + " `" + formatTime(audioTrack.getPosition()) + "/" + formatTime(audioTrack.getDuration()) + "` \nAuthor: " + audioTrack.getInfo().author);
            messageBuilder.setEmbeds(embedBuilder.build());

            return messageBuilder;
        } else {
            MessageBuilder messageBuilder = new MessageBuilder();
            EmbedBuilder noMusicPlaying = new EmbedBuilder()
                    .setTitle("No music playing")
                    .setDescription(":stop_button: ▬▬▬▬▬▬▬▬▬▬ `--:--:--/--:--:--` \nAuthor: ---");
            messageBuilder.setEmbeds(noMusicPlaying.build());
            return messageBuilder;
        }
    }

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
}
