package net.jandie1505.musicbot.system;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jandie1505.musicbot.search.YTSearchHandler;

import java.awt.*;
import java.util.List;
import java.util.Random;
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
                        String playIcon = ":stop_button:";
                        String progressbar = "▬▬▬▬▬▬▬▬▬▬";

                        if(MusicManager.isPaused(event.getGuild())) {
                            playIcon = ":pause_button:";
                            progressbar = getProgressBar(audioTrack.getPosition(), audioTrack.getDuration());
                        } else if(!MusicManager.isPaused(event.getGuild())) {
                            playIcon = ":arrow_forward:";
                            progressbar = getProgressBar(audioTrack.getPosition(), audioTrack.getDuration());
                        }

                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle(audioTrack.getInfo().title, audioTrack.getInfo().uri)
                                .setDescription(playIcon + " " + progressbar + " `" + formatTime(audioTrack.getPosition()) + "/" + formatTime(audioTrack.getDuration()) + "` \nAuthor: " + audioTrack.getInfo().author);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    } else {
                        EmbedBuilder noMusicPlaying = new EmbedBuilder()
                                .setTitle("No music playing")
                                .setDescription(":stop_button: ▬▬▬▬▬▬▬▬▬▬ `--:--:--/--:--:--` \nAuthor: ---");
                        event.getHook().sendMessage("").addEmbeds(noMusicPlaying.build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("queue")) {
                if(GMS.memberHasUserPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(!MusicManager.getQueue(event.getGuild()).isEmpty()) {
                        if(event.getOption("index") != null) {
                            int queueIndex = (int) event.getOption("index").getAsLong();
                            String queue = "";
                            int index = 0;
                            for(AudioTrack track : MusicManager.getQueue(event.getGuild())) {
                                String current = "`" + index + ".` " +
                                        "`" + formatTime(track.getDuration()) + "` " +
                                        track.getInfo().title + " [" + track.getInfo().author + "]\n";
                                String nextString = queue + current;
                                if(nextString.length() <= 950) {
                                    if(index >= queueIndex) {
                                        queue = queue + current;
                                    }
                                } else {
                                    queue = queue + "`+ " + (MusicManager.getQueue(event.getGuild()).size()-index) + " entries. Use /queue <index> to search for indexes.`";
                                    break;
                                }
                                index++;
                            }
                            EmbedBuilder queueFull = new EmbedBuilder()
                                    .addField("Queue:", queue, false);
                            event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                        } else {
                            String queue = "";
                            int index = 0;
                            for(AudioTrack track : MusicManager.getQueue(event.getGuild())) {
                                String current = "`" + index + ".` " +
                                        "`" + formatTime(track.getDuration()) + "` " +
                                        track.getInfo().title + " [" + track.getInfo().author + "]\n";
                                String nextString = queue + current;
                                if(nextString.length() <= 950) {
                                    queue = queue + current;
                                } else {
                                    queue = queue + "`+ " + (MusicManager.getQueue(event.getGuild()).size()-index) + " entries. Use /queue <index> to search for indexes.`";
                                    break;
                                }
                                index++;
                            }
                            EmbedBuilder queueFull = new EmbedBuilder()
                                    .addField("Queue:", queue, false);
                            event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                        }
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
                            MusicManager.connect(event.getMember().getVoiceState().getChannel());
                            this.play(event, true);
                        } else {
                            EmbedBuilder notInVoiceChannel = new EmbedBuilder()
                                    .setDescription(":warning:  You are not in a voice channel")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(notInVoiceChannel.build()).queue();
                        }
                    } else {
                        if(!MusicManager.isPaused(event.getGuild()) && MusicManager.getPlayingTrack(event.getGuild()) == null) {
                            this.play(event, true);
                        } else {
                            this.play(event, false);
                        }
                    }
                }
            } else if(event.getName().equalsIgnoreCase("add")) {
                if(GMS.memberHasUserPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        if(event.getOption("song") != null) {
                            MusicManager.add(event.getGuild(), event.getOption("song").getAsString(), event, false);
                        } else {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":warning:  Song required")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    } else {
                        event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
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
            } else if(event.getName().equalsIgnoreCase("pause")) {
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
                        MusicManager.disconnect(event.getGuild());
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
                        event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("remove")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        if(event.getOption("index") != null) {
                            int index = (int) event.getOption("index").getAsLong();
                            if(index < MusicManager.getQueue(event.getGuild()).size()) {
                                MusicManager.remove(event.getGuild(), index);
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":white_check_mark:  Successfully removed")
                                        .setColor(Color.GREEN);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Index does not exist")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":warning:  Index required")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    } else {
                        event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("clear")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        MusicManager.clear(event.getGuild());
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":asterisk:  Queue cleared")
                                .setColor(Color.GREEN);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    } else {
                        event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
                    }
                }
            }
            else if(event.getName().equalsIgnoreCase("movetrack")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        if(event.getOption("from") != null && event.getOption("to") != null) {
                            int from = (int) event.getOption("from").getAsLong();
                            int to = (int) event.getOption("to").getAsLong();
                            if(from < MusicManager.getQueue(event.getGuild()).size()) {
                                MusicManager.move(event.getGuild(), from, to);
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":white_check_mark:  Successfully moved")
                                        .setColor(Color.GREEN);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  From index does not exist")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":warning:  Indexes does not exist")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    } else {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":warning:  Not connected")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("shuffle")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        if(!MusicManager.getQueue(event.getGuild()).isEmpty()) {
                            MusicManager.shuffle(event.getGuild());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":twisted_rightwards_arrows:  Shuffled queue")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":warning:  Queue is empty")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    } else {
                        event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("playnow")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(event.getOption("song") != null) {
                        if(!MusicManager.isConnected(event.getGuild())) {
                            if(event.getMember().getVoiceState().inVoiceChannel()) {
                                MusicManager.connect(event.getMember().getVoiceState().getChannel());
                                this.playnow(event);
                            } else {
                                EmbedBuilder notInVoiceChannel = new EmbedBuilder()
                                        .setDescription(":warning:  You are not in a voice channel")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(notInVoiceChannel.build()).queue();
                            }
                        } else {
                            this.playnow(event);
                        }
                    } else {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":warning:  Music source required")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("search")) {
                if(GMS.memberHasUserPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(event.getOption("query") != null) {
                        List<AudioTrack> trackList = YTSearchHandler.search(event.getOption("query").getAsString());
                        String returnString = "";
                        int index = 0;
                        for(AudioTrack track : trackList) {
                            String current = "`" + index + ".` `" + formatTime(track.getInfo().length) + "` " + track.getInfo().title + " [" + track.getInfo().author + "]\n";
                            String stringAfter = returnString + current;
                            if(!(stringAfter.length() > 1000)) {
                                returnString = returnString + "`" + index + ".` `" + formatTime(track.getInfo().length) + "` " + track.getInfo().title + " [" + track.getInfo().author + "]\n";
                            }
                            index++;
                        }
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .addField("Search result:", returnString, false);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    } else {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":warning:  Search query required")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("volume")) {
                if(GMS.memberHasDJPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        if(event.getOption("volume") != null) {
                            int volume = (int) event.getOption("volume").getAsLong();
                            if((volume <= 200) && (volume >= 0)) {
                                MusicManager.setVolume(event.getGuild(), volume);
                                if(volume == 200) {
                                    Random randomizer = new Random();
                                    int random = randomizer.nextInt(3);
                                    if(random == 2) {
                                        event.getHook().sendMessage("https://tenor.com/view/nuclear-catastrophic-disastrous-melt-down-gif-13918708").queue();
                                    } else {
                                        event.getHook().sendMessage(":exploding_head:").queue();
                                    }
                                } else if(volume >= 150) {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":loud_sound: :boom:  " + volume)
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else if(volume >= 100) {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":loud_sound:  " + volume)
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else if(volume >= 1) {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":sound:  " + volume)
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else if(volume == 0) {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":mute:  " + volume)
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":speaker:  " + volume)
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                if(volume > 200) {
                                    Random randomizer = new Random();
                                    int random = randomizer.nextInt(3);
                                    if(random == 2) {
                                        event.getHook().sendMessage("The volume has been capped at 200 to avoid this:\nhttps://tenor.com/view/explosion-mushroom-cloud-atomic-bomb-bomb-boom-gif-4464831").queue();
                                    } else {
                                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                                .setDescription("The volume has been capped at 200 so you don't end up like this guy: :exploding_head:")
                                                .setColor(Color.RED);
                                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                    }
                                } else if(volume < 0) {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription("Active noise cancelling is unfortunately not supported :(")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            }
                        } else {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription("Current volume is: " + MusicManager.getVolume(event.getGuild()));
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    } else {
                        event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("skip")) {
                if(GMS.memberHasUserPermissions(event.getMember())) {
                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                    if(MusicManager.isConnected(event.getGuild())) {
                        if(MusicManager.getSkipvotes(event.getGuild()).contains(event.getMember())) {
                            MusicManager.removeSkipvote(event.getGuild(), event.getMember());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":negative_squared_cross_mark: Removed skipvote (" + (MusicManager.getVoteCount(event.getGuild())-1) + "/" + ((event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size()-1)/2) + ")")
                                    .setColor(Color.YELLOW);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else {
                            MusicManager.addSkipvote(event.getGuild(), event.getMember());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":fast_forward:  You have voted to skip (" + (MusicManager.getVoteCount(event.getGuild())+1) + "/" + ((event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size()-1)/2) + ")")
                                    .setColor(Color.YELLOW);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    } else {
                        event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
                    }
                }
            } else if(event.getName().equalsIgnoreCase("mbsettings")) {
                if(GMS.memberHasAdminPermissions(event.getMember())) {
                    if(event.getSubcommandName() != null) {
                        if(event.getSubcommandName().equalsIgnoreCase("info")) {
                            event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                            String djroles = "";
                            for(String DJRoleId : GMS.getDJRoles(event.getGuild().getId())) {
                                Role role = event.getGuild().getRoleById(DJRoleId);
                                if(role != null) {
                                    djroles = djroles + role.getName() + " (" + role.getId() + ")\n";
                                }
                            }
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setTitle("MusicBot Settings")
                                    .setDescription("MusicBot by jandie1505")
                                    .addField("DJ Roles:", djroles, false)
                                    .addField("Ephemeral replies:", String.valueOf(DatabaseManager.getEphemeralState(event.getGuild().getId())), false);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else if(event.getSubcommandName().equalsIgnoreCase("djrole")) {
                            if(event.getOption("action") != null) {
                                if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                                    GMS.reloadDJRoles(event.getGuild().getId());
                                    if(event.getOption("role") != null) {
                                        GMS.addDJRole(event.getGuild().getId(), event.getOption("role").getAsRole().getId());
                                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                                .setDescription(":white_check_mark:  Added DJ Role")
                                                .setColor(Color.GREEN);
                                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                    } else {
                                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                                .setDescription(":warning:  Role required")
                                                .setColor(Color.RED);
                                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                    }
                                } else if(event.getOption("action").getAsString().equalsIgnoreCase("remove")) {
                                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                                    GMS.reloadDJRoles(event.getGuild().getId());
                                    if(event.getOption("role") != null) {
                                        GMS.removeDJRole(event.getGuild().getId(), event.getOption("role").getAsRole().getId());
                                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                                .setDescription(":white_check_mark:  Removed DJ Role")
                                                .setColor(Color.GREEN);
                                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                    } else {
                                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                                .setDescription(":warning:  Role required")
                                                .setColor(Color.RED);
                                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                    }
                                } else if(event.getOption("action").getAsString().equalsIgnoreCase("clear")) {
                                    event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                                    GMS.clearDJRoles(event.getGuild().getId());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Cleared DJ Roles")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            }
                        } else if(event.getSubcommandName().equalsIgnoreCase("ephemeral")) {
                            if(event.getOption("state") != null) {
                                event.deferReply(DatabaseManager.getEphemeralState(event.getGuild().getId())).queue();
                                DatabaseManager.setEphemeralState(event.getGuild().getId(), event.getOption("state").getAsBoolean());
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":white_check_mark:  Set ephemeral state to " + event.getOption("state").getAsBoolean())
                                        .setColor(Color.GREEN);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        }
                    }
                }
            }
        }
        if(event.getName().equalsIgnoreCase("cmd")) {
            event.deferReply(true).queue();
            if(event.getOption("cmd") != null) {
                String response = ""; /*Commands.command(event.getOption("cmd").getAsString());*/
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .addField("Command response:", response, false);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            } else {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(":warning:  Command required")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    private void play(SlashCommandEvent event, boolean startafterload) {
        if(event.getOption("song") != null) {
            String source = event.getOption("song").getAsString();
            if(source.startsWith("http://") || source.startsWith("https://")) {
                MusicManager.add(event.getGuild(), source, event, startafterload);
            } else {
                List<AudioTrack> trackList = YTSearchHandler.search(source);
                if(!trackList.isEmpty()) {
                    MusicManager.add(event.getGuild(), trackList.get(0).getInfo().uri, event, startafterload);
                } else {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":warning:  Nothing was found")
                            .setColor(Color.RED);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                }
            }
        } else {
            if(GMS.memberHasDJPermissions(event.getMember())) {
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
            } else {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(":x:  No permission")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            }
        }
    }
    private void playnow(SlashCommandEvent event) {
        if(event.getOption("song") != null) {
            String source = event.getOption("song").getAsString();
            if(source.startsWith("http://") || source.startsWith("https://")) {
                MusicManager.playnow(event.getGuild(), source, event);
            } else {
                List<AudioTrack> trackList = YTSearchHandler.search(source);
                if(!trackList.isEmpty()) {
                    MusicManager.playnow(event.getGuild(), trackList.get(0).getInfo().uri, event);
                } else {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":warning:  Nothing was found")
                            .setColor(Color.RED);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                }
            }
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setDescription(":warning:  Music source required")
                    .setColor(Color.RED);
            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
        }
    }

    private EmbedBuilder getNotConnectedErrorMessage(){
        return new EmbedBuilder()
                .setDescription(":warning:  Not connected")
                .setColor(Color.RED);
    }

    private String formatTime(long millis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private String getProgressBar(double position, double duration) {
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
