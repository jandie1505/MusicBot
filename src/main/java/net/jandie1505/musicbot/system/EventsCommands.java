package net.jandie1505.musicbot.system;

import com.sedmelluq.discord.lavaplayer.player.event.*;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.lang3.StringUtils;

import javax.sound.midi.Track;
import java.awt.*;
import java.lang.reflect.Member;
import java.util.concurrent.TimeUnit;

public class EventsCommands extends ListenerAdapter {
    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if(event.getMember() != null && event.getGuild() != null) {
            if(event.getName().equalsIgnoreCase("nowplaying")) {
                if(GMS.memberHasUserPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.getPlayingTrack(event.getGuild()) != null) {
                        AudioTrack audioTrack = MusicManager.getPlayingTrack(event.getGuild());
                        String description = "";
                        if(MusicManager.isPaused(event.getGuild())) {
                            description = ":pause_button:  Player is currently paused";
                        } else {
                            description = ":arrow_forward:  Player is currently playing";
                        }
                        EmbedBuilder musicPlaying = new EmbedBuilder()
                                .setDescription(description)
                                .addField("**Title: **", audioTrack.getInfo().title, false)
                                .addField("**Author: **", audioTrack.getInfo().author, false)
                                .addField("**Stream: **", Boolean.toString(audioTrack.getInfo().isStream), false)
                                .addField("**Duration: **", audioTrack.getPosition() + "/" + audioTrack.getDuration(), false);
                        event.getHook().sendMessage("").addEmbeds(musicPlaying.build()).queue();
                    } else {
                        EmbedBuilder noMusicPlaying = new EmbedBuilder()
                                .setDescription("Currently is no music playing")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(noMusicPlaying.build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("queue")) {
                if(GMS.memberHasUserPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(!MusicManager.getQueue(event.getGuild()).isEmpty()) {
                        String queue = "";
                        int index = 0;
                        for(AudioTrack track : MusicManager.getQueue(event.getGuild())) {
                            long millis = track.getDuration();
                            queue = queue + "`" + index + ".` " +
                                    "`" + String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))) + "` " +
                                    track.getInfo().title + " [" + track.getInfo().author + "]\n";
                            index++;
                        }
                        EmbedBuilder queueFull = new EmbedBuilder()
                                .addField("Queue:", queue, false);
                        event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                    } else {
                        EmbedBuilder queueEmpty = new EmbedBuilder()
                                .setDescription("The queue is empty")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(queueEmpty.build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("play")) {
                if(GMS.memberHasUserPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(!MusicManager.isConnected(event.getGuild())) {
                        if(event.getMember().getVoiceState().inVoiceChannel()) {
                            MusicManager.joinVoiceChannel(event.getMember().getVoiceState().getChannel());
                            this.play(event);
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {}
                            MusicManager.next(event.getGuild());
                        } else {
                            EmbedBuilder notInVoiceChannel = new EmbedBuilder()
                                    .setDescription(":warning:  You are not in a voice channel")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(notInVoiceChannel.build()).queue();
                        }
                    } else {
                        this.play(event);
                    }
                }
            } else if(event.getName().equalsIgnoreCase("stop")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        if(!MusicManager.isPaused(event.getGuild())) {
                            MusicManager.setPause(event.getGuild(), true, new AudioEventListener() {
                                @Override
                                public void onEvent(AudioEvent audioEvent) {
                                    if(audioEvent instanceof PlayerPauseEvent) {
                                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                                .setDescription(":pause_button:  Player paused")
                                                .setColor(Color.GREEN);
                                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                    }
                                }
                            });
                        } else {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription("Player is already paused")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    } else {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription("Bot is not playing")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("leave")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        MusicManager.clear(event.getGuild());
                        MusicManager.stop(event.getGuild());
                        MusicManager.leaveVoiceChannel(event.getGuild());
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":white_check_mark:  Left voice channel")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    } else {
                        MusicManager.clear(event.getGuild());
                        MusicManager.stop(event.getGuild());
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription("Already not connected")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("forceskip")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        if(event.getOption("position") != null) {
                            if(MusicManager.getPlayingTrack(event.getGuild()) != null) {
                                String previousSong = "";
                                previousSong = MusicManager.getPlayingTrack(event.getGuild()).getInfo().title;
                                int position = (int) event.getOption("position").getAsLong();
                                MusicManager.next(event.getGuild(), position);
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":track_next:  Skipped " + previousSong)
                                        .setColor(Color.GREEN);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Nothing to skip")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else {
                            if(MusicManager.getPlayingTrack(event.getGuild()) != null) {
                                String previousSong = "";
                                previousSong = MusicManager.getPlayingTrack(event.getGuild()).getInfo().title;
                                MusicManager.next(event.getGuild());
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":track_next:  Skipped " + previousSong)
                                        .setColor(Color.GREEN);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Nothing to skip")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        }
                    } else {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":warning:  Not connected")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                }
            }
        }
    }

    private void play(SlashCommandEvent event) {
        if(event.getOption("song") != null) {
            String source = event.getOption("song").getAsString();
            if(source.startsWith("http://") || source.startsWith("https://")) {
                MusicManager.add(event.getGuild(), source, event);
            } else {
                EmbedBuilder notSupportedMessage = new EmbedBuilder()
                        .setDescription(":warning:  Currently not supported")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(notSupportedMessage.build()).queue();
            }
        } else {
            if(!MusicManager.getQueue(event.getGuild()).isEmpty() || MusicManager.isPaused(event.getGuild()) || MusicManager.getPlayingTrack(event.getGuild()) != null) {
                if(MusicManager.isPaused(event.getGuild())) {
                    MusicManager.setPause(event.getGuild(), false);
                }
                if(MusicManager.getPlayingTrack(event.getGuild()) == null) {
                    MusicManager.next(event.getGuild());
                }
                EmbedBuilder resumedMessage = new EmbedBuilder()
                        .setDescription(":arrow_forward:  Resumed playback")
                        .setColor(Color.GREEN);
                event.getHook().sendMessage("").addEmbeds(resumedMessage.build()).queue();
            } else {
                EmbedBuilder resumedMessage = new EmbedBuilder()
                        .setDescription(":warning:  Nothing playing")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(resumedMessage.build()).queue();
            }
        }
    }
}
