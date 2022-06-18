package net.jandie1505.musicbot.eventlisteners;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.Commands;
import net.jandie1505.musicbot.utilities.SpotifySearchHandler;
import net.jandie1505.musicbot.utilities.YTSearchHandler;
import net.jandie1505.musicbot.utilities.Messages;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EventsCommands extends ListenerAdapter {
    
    private MusicBot musicBot;
    
    public EventsCommands(MusicBot musicBot) {
        this.musicBot = musicBot;
    }
    
    // EVENT
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getMember() != null && event.getGuild() != null) {
            if(event.getName().equalsIgnoreCase("nowplaying")) {
                this.nowplayingCommand(event);
            } else if(event.getName().equalsIgnoreCase("queue")) {
                this.queueCommand(event);
            } else if(event.getName().equalsIgnoreCase("play")) {
                this.playCommand(event);
            } else if(event.getName().equalsIgnoreCase("add")) {
                this.addCommand(event);
            } else if(event.getName().equalsIgnoreCase("stop")) {
                this.stopCommand(event);
            } else if(event.getName().equalsIgnoreCase("pause")) {
                this.stopCommand(event);
            } else if(event.getName().equalsIgnoreCase("leave")) {
                this.leaveCommand(event);
            } else if(event.getName().equalsIgnoreCase("forceskip")) {
                this.forceskipCommand(event);
            } else if(event.getName().equalsIgnoreCase("remove")) {
                this.removeCommand(event);
            } else if(event.getName().equalsIgnoreCase("clear")) {
                this.clearCommand(event);
            } else if(event.getName().equalsIgnoreCase("movetrack")) {
                this.movetrackCommand(event);
            } else if(event.getName().equalsIgnoreCase("shuffle")) {
                this.shuffleCommand(event);
            } else if(event.getName().equalsIgnoreCase("playnow")) {
                this.playnowCommand(event);
            } else if(event.getName().equalsIgnoreCase("search")) {
                this.searchCommand(event);
            } else if(event.getName().equalsIgnoreCase("volume")) {
                this.volumeCommand(event);
            } else if(event.getName().equalsIgnoreCase("skip")) {
                this.skipCommand(event);
            } else if(event.getName().equalsIgnoreCase("mbsettings")) {
                this.mbsettingsCommand(event);
            }
        }
        if(event.getName().equalsIgnoreCase("cmd")) {
            this.cmdCommand(event);
        } else if(event.getName().equalsIgnoreCase("help")) {
            this.helpCommand(event);
        }
    }

    // COMMANDS
    private void nowplayingCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasUserPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            event.getHook().sendMessage(Messages.nowplayingMessage(musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build()).queue();
        }
    }

    private void queueCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasUserPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(!this.musicBot.getMusicManager().getQueue(event.getGuild()).isEmpty()) {
                if(event.getOption("index") != null) {
                    int queueIndex = (int) event.getOption("index").getAsLong();
                    String queue = "";
                    int index = 0;
                    for(AudioTrack track : this.musicBot.getMusicManager().getQueue(event.getGuild())) {
                        String current = "`" + index + ".` " +
                                "`" + Messages.formatTime(track.getDuration()) + "` " +
                                track.getInfo().title + " [" + track.getInfo().author + "]\n";
                        String nextString = queue + current;
                        if(nextString.length() <= 950) {
                            if(index >= queueIndex) {
                                queue = queue + current;
                            }
                        } else {
                            queue = queue + "`+ " + (this.musicBot.getMusicManager().getQueue(event.getGuild()).size()-index) + " entries. Use /queue <index> to search for indexes.`";
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
                    for(AudioTrack track : this.musicBot.getMusicManager().getQueue(event.getGuild())) {
                        String current = "`" + index + ".` " +
                                "`" + Messages.formatTime(track.getDuration()) + "` " +
                                track.getInfo().title + " [" + track.getInfo().author + "]\n";
                        String nextString = queue + current;
                        if(nextString.length() <= 950) {
                            queue = queue + current;
                        } else {
                            queue = queue + "`+ " + (this.musicBot.getMusicManager().getQueue(event.getGuild()).size()-index) + " entries. Use /queue <index> to search for indexes.`";
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
    }

    private void playCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasUserPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(!this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(event.getMember().getVoiceState().inAudioChannel()) {
                    if(this.musicBot.getMusicManager().connect(event.getMember().getVoiceState().getChannel())) {
                        this.play(event, true);
                    } else {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":warning:  Can't connect to voice channel (Missing permissions?)")
                                .setColor(Color.RED);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                } else {
                    EmbedBuilder notInVoiceChannel = new EmbedBuilder()
                            .setDescription(":warning:  You are not in a voice channel")
                            .setColor(Color.RED);
                    event.getHook().sendMessage("").addEmbeds(notInVoiceChannel.build()).queue();
                }
            } else {
                if(!this.musicBot.getMusicManager().isPaused(event.getGuild()) && this.musicBot.getMusicManager().getPlayingTrack(event.getGuild()) == null) {
                    this.play(event, true);
                } else {
                    this.play(event, false);
                }
            }
        }
    }

    private void addCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasUserPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(event.getOption("song") != null) {
                    this.musicBot.getMusicManager().add(event.getGuild(), event.getOption("song").getAsString(), event, false);
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
    }

    private void stopCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(!this.musicBot.getMusicManager().isPaused(event.getGuild())) {
                    this.musicBot.getMusicManager().setPause(event.getGuild(), true, new AudioEventListener() {
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
    }

    private void leaveCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                this.musicBot.getMusicManager().clear(event.getGuild());
                this.musicBot.getMusicManager().stop(event.getGuild());
                this.musicBot.getMusicManager().disconnect(event.getGuild());
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(":white_check_mark:  Left voice channel")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            } else {
                this.musicBot.getMusicManager().clear(event.getGuild());
                this.musicBot.getMusicManager().stop(event.getGuild());
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription("Already not connected")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    private void forceskipCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(event.getOption("position") != null) {
                    if(this.musicBot.getMusicManager().getPlayingTrack(event.getGuild()) != null) {
                        String previousSong = "";
                        previousSong = this.musicBot.getMusicManager().getPlayingTrack(event.getGuild()).getInfo().title;
                        int position = (int) event.getOption("position").getAsLong();
                        this.musicBot.getMusicManager().next(event.getGuild(), position);
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
                    if(this.musicBot.getMusicManager().getPlayingTrack(event.getGuild()) != null) {
                        String previousSong = "";
                        previousSong = this.musicBot.getMusicManager().getPlayingTrack(event.getGuild()).getInfo().title;
                        this.musicBot.getMusicManager().next(event.getGuild());
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
    }

    private void removeCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(event.getOption("index") != null) {
                    int index = (int) event.getOption("index").getAsLong();
                    if(index < this.musicBot.getMusicManager().getQueue(event.getGuild()).size()) {
                        this.musicBot.getMusicManager().remove(event.getGuild(), index);
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
    }

    private void clearCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                this.musicBot.getMusicManager().clear(event.getGuild());
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(":asterisk:  Queue cleared")
                        .setColor(Color.GREEN);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            } else {
                event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
            }
        }
    }

    private void movetrackCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(event.getOption("from") != null && event.getOption("to") != null) {
                    int from = (int) event.getOption("from").getAsLong();
                    int to = (int) event.getOption("to").getAsLong();
                    if(from < this.musicBot.getMusicManager().getQueue(event.getGuild()).size()) {
                        this.musicBot.getMusicManager().move(event.getGuild(), from, to);
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
    }

    private void shuffleCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(!this.musicBot.getMusicManager().getQueue(event.getGuild()).isEmpty()) {
                    this.musicBot.getMusicManager().shuffle(event.getGuild());
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
    }

    private void playnowCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(event.getOption("song") != null) {
                if(!this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                    if(event.getMember().getVoiceState().inAudioChannel()) {
                        this.musicBot.getMusicManager().connect(event.getMember().getVoiceState().getChannel());
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
    }

    private void searchCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasUserPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(event.getOption("query") != null) {
                List<AudioTrack> trackList = YTSearchHandler.search(event.getOption("query").getAsString());
                String returnString = "";
                int index = 0;
                for(AudioTrack track : trackList) {
                    String current = "`" + index + ".` `" + Messages.formatTime(track.getInfo().length) + "` " + track.getInfo().title + " [" + track.getInfo().author + "]\n";
                    String stringAfter = returnString + current;
                    if(!(stringAfter.length() > 1000)) {
                        returnString = returnString + "`" + index + ".` `" + Messages.formatTime(track.getInfo().length) + "` " + track.getInfo().title + " [" + track.getInfo().author + "]\n";
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
    }

    private void volumeCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(System.getProperty("os.arch").equalsIgnoreCase("amd64")) {
                if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                    if(event.getOption("volume") != null) {
                        int volume = (int) event.getOption("volume").getAsLong();
                        if((volume <= 200) && (volume >= 0)) {
                            this.musicBot.getMusicManager().setVolume(event.getGuild(), volume);
                            if(volume == 200) {
                                Random randomizer = new Random();
                                int random = randomizer.nextInt(9);
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
                                int random = randomizer.nextInt(9);
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
                                .setDescription("Current volume is: " + this.musicBot.getMusicManager().getVolume(event.getGuild()));
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                } else {
                    event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
                }
            } else {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setDescription(":warning:  The bot is hosted on a device that does not support this command")
                        .setColor(Color.RED);
                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    private void skipCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasUserPermissions(event.getMember())) {
            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
            if(this.musicBot.getMusicManager().isConnected(event.getGuild())) {
                if(this.musicBot.getMusicManager().getSkipvotes(event.getGuild()).contains(event.getMember())) {
                    this.musicBot.getMusicManager().removeSkipvote(event.getGuild(), event.getMember());
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":negative_squared_cross_mark: Removed skipvote (" + (this.musicBot.getMusicManager().getVoteCount(event.getGuild())-1) + "/" + ((event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size()-1)/2) + ")")
                            .setColor(Color.YELLOW);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                } else {
                    this.musicBot.getMusicManager().addSkipvote(event.getGuild(), event.getMember());
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":fast_forward:  You have voted to skip (" + (this.musicBot.getMusicManager().getVoteCount(event.getGuild())+1) + "/" + ((event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size()-1)/2) + ")")
                            .setColor(Color.YELLOW);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                }
            } else {
                event.getHook().sendMessage("").addEmbeds(getNotConnectedErrorMessage().build()).queue();
            }
        }
    }

    private void mbsettingsCommand(SlashCommandInteractionEvent event) {
        if(this.musicBot.getGMS().memberHasAdminPermissions(event.getMember())) {
            if(event.getSubcommandName() != null) {
                if(event.getSubcommandName().equalsIgnoreCase("info")) {
                    event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                    String djroles = "";
                    for(String DJRoleId : this.musicBot.getGMS().getDJRoles(event.getGuild().getId())) {
                        Role role = event.getGuild().getRoleById(DJRoleId);
                        if(role != null) {
                            djroles = djroles + role.getName() + " (" + role.getId() + ")\n";
                        }
                    }
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle("MusicBot Settings")
                            .setDescription("MusicBot by jandie1505")
                            .addField("DJ Roles:", djroles, false)
                            .addField("Ephemeral replies:", String.valueOf(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())), false)
                            .addField("Blacklisted tracks:", String.valueOf(this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId()).size()) + " blacklisted tracks\n" + String.valueOf(this.musicBot.getDatabaseManager().getGlobalBlacklist().size()) + " global blacklisted tracks", false);
                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                } else if(event.getSubcommandName().equalsIgnoreCase("djrole")) {
                    if(event.getOption("action") != null) {
                        if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            this.musicBot.getGMS().reloadDJRoles(event.getGuild().getId());
                            if(event.getOption("role") != null) {
                                this.musicBot.getGMS().addDJRole(event.getGuild().getId(), event.getOption("role").getAsRole().getId());
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
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            this.musicBot.getGMS().reloadDJRoles(event.getGuild().getId());
                            if(event.getOption("role") != null) {
                                this.musicBot.getGMS().removeDJRole(event.getGuild().getId(), event.getOption("role").getAsRole().getId());
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
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            this.musicBot.getGMS().clearDJRoles(event.getGuild().getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Cleared DJ Roles")
                                    .setColor(Color.GREEN);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    }
                } else if(event.getSubcommandName().equalsIgnoreCase("ephemeral")) {
                    if(event.getOption("state") != null) {
                        event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                        this.musicBot.getDatabaseManager().setEphemeralState(event.getGuild().getId(), event.getOption("state").getAsBoolean());
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setDescription(":white_check_mark:  Set ephemeral state to " + event.getOption("state").getAsBoolean())
                                .setColor(Color.GREEN);
                        event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                    }
                } else if(event.getSubcommandName().equalsIgnoreCase("blacklist")) {
                    if(event.getOption("action") != null) {
                        if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            if(event.getOption("link") != null) {
                                if(!this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId()).contains(event.getOption("link").getAsString())) {
                                    this.musicBot.getDatabaseManager().addToBlacklist(event.getGuild().getId(), event.getOption("link").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Added link to blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already added to blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Link required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("remove")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            if(event.getOption("link") != null) {
                                if(this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId()).contains(event.getOption("link").getAsString())) {
                                    this.musicBot.getDatabaseManager().deleteFromBlacklist(event.getGuild().getId(), event.getOption("link").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Removed link from blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already not in blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Link required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("clear")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            this.musicBot.getDatabaseManager().clearBlacklist(event.getGuild().getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Cleared blacklist")
                                    .setColor(Color.GREEN);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("list")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            String blacklist = "";
                            int index = 0;
                            for(String string : this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId())) {
                                String current = string + "\n";
                                String nextString = blacklist + current;
                                if(nextString.length() <= 950) {
                                    blacklist = blacklist + current;
                                } else {
                                    blacklist = blacklist + "`+ " + (this.musicBot.getDatabaseManager().getBlacklist(event.getGuild().getId()).size()-index) + " entries.`";
                                    break;
                                }
                                index++;
                            }
                            EmbedBuilder queueFull = new EmbedBuilder()
                                    .addField("Blacklist:", blacklist, false);
                            event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                        }
                    }
                } else if(event.getSubcommandName().equalsIgnoreCase("keywordblacklist")) {
                    if(event.getOption("action") != null) {
                        if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            if(event.getOption("keyword") != null) {
                                if(!this.musicBot.getDatabaseManager().getKeywordBlacklist(event.getGuild().getId()).contains(event.getOption("keyword").getAsString())) {
                                    this.musicBot.getDatabaseManager().addToBlacklist(event.getGuild().getId(), event.getOption("keyword").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Added keyword to blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already added to keyword blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Keyword required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("remove")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            if(event.getOption("keyword") != null) {
                                if(this.musicBot.getDatabaseManager().getKeywordBlacklist(event.getGuild().getId()).contains(event.getOption("keyword").getAsString())) {
                                    this.musicBot.getDatabaseManager().deleteFromBlacklist(event.getGuild().getId(), event.getOption("keyword").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Removed keyword from blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already not in keyword blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Keyword required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("clear")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            this.musicBot.getDatabaseManager().clearBlacklist(event.getGuild().getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Cleared keyword blacklist")
                                    .setColor(Color.GREEN);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("list")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            String blacklist = "";
                            int index = 0;
                            for(String string : this.musicBot.getDatabaseManager().getKeywordBlacklist(event.getGuild().getId())) {
                                String current = string + "\n";
                                String nextString = blacklist + current;
                                if(nextString.length() <= 950) {
                                    blacklist = blacklist + current;
                                } else {
                                    blacklist = blacklist + "`+ " + (this.musicBot.getDatabaseManager().getKeywordBlacklist(event.getGuild().getId()).size()-index) + " entries.`";
                                    break;
                                }
                                index++;
                            }
                            EmbedBuilder queueFull = new EmbedBuilder()
                                    .addField("Keyword Blacklist:", blacklist, false);
                            event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                        }
                    }
                } else if(event.getSubcommandName().equalsIgnoreCase("artistblacklist")) {
                    if(event.getOption("action") != null) {
                        if(event.getOption("action").getAsString().equalsIgnoreCase("add")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            if(event.getOption("artist") != null) {
                                if(!this.musicBot.getDatabaseManager().getArtistBlacklist(event.getGuild().getId()).contains(event.getOption("artist").getAsString())) {
                                    this.musicBot.getDatabaseManager().addToBlacklist(event.getGuild().getId(), event.getOption("artist").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Added artist to blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already added to artist blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Artist required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("remove")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            if(event.getOption("artist") != null) {
                                if(this.musicBot.getDatabaseManager().getArtistBlacklist(event.getGuild().getId()).contains(event.getOption("artist").getAsString())) {
                                    this.musicBot.getDatabaseManager().deleteFromBlacklist(event.getGuild().getId(), event.getOption("artist").getAsString());
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Removed artist from blacklist")
                                            .setColor(Color.GREEN);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setDescription(":warning:  Already not in artist blacklist")
                                            .setColor(Color.RED);
                                    event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Artist required")
                                        .setColor(Color.RED);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("clear")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            this.musicBot.getDatabaseManager().clearBlacklist(event.getGuild().getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":white_check_mark:  Cleared artist blacklist")
                                    .setColor(Color.GREEN);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        } else if(event.getOption("action").getAsString().equalsIgnoreCase("list")) {
                            event.deferReply(this.musicBot.getDatabaseManager().getEphemeralState(event.getGuild().getId())).queue();
                            String blacklist = "";
                            int index = 0;
                            for(String string : this.musicBot.getDatabaseManager().getArtistBlacklist(event.getGuild().getId())) {
                                String current = string + "\n";
                                String nextString = blacklist + current;
                                if(nextString.length() <= 950) {
                                    blacklist = blacklist + current;
                                } else {
                                    blacklist = blacklist + "`+ " + (this.musicBot.getDatabaseManager().getArtistBlacklist(event.getGuild().getId()).size()-index) + " entries.`";
                                    break;
                                }
                                index++;
                            }
                            EmbedBuilder queueFull = new EmbedBuilder()
                                    .addField("Artist Blacklist:", blacklist, false);
                            event.getHook().sendMessage("").addEmbeds(queueFull.build()).queue();
                        }
                    }
                }
            }
        }
    }

    private void cmdCommand(SlashCommandInteractionEvent event) {
        if(event.getMember().getId().equals(this.musicBot.getConfigManager().getConfig().getBotOwner())) {
            event.deferReply(true).queue();
            if(event.getOption("cmd") != null) {
                String response = Commands.command(musicBot, event.getOption("cmd").getAsString());
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

    private void helpCommand(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        event.getHook().sendMessage("").addEmbeds(getHelpMessage().build()).queue();
    }



    // UTILITY
    private void play(SlashCommandInteractionEvent event, boolean startafterload) {
        if(event.getOption("song") != null) {
            String source = event.getOption("song").getAsString();
            if(source.startsWith("http://") || source.startsWith("https://")) {
                if(source.contains("https://www.youtube.com/") || source.contains("https://youtube.com/")) {
                    this.musicBot.getMusicManager().add(event.getGuild(), source, event, startafterload);
                } else if(source.contains("https://open.spotify.com/playlist/")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Random randomizer = new Random();
                            int random = randomizer.nextInt(9);
                            if(random == 8) {
                                event.getHook().sendMessage("Loading spotify playlist. Here is a gif to show you how long this can take:\nhttps://tenor.com/view/loading-forever-12years-later-gif-10516198").queue();
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":zzz:  Loading playlist from spotify...")
                                        .setColor(Color.YELLOW);
                                event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                            }
                            List<AudioTrack> trackList = SpotifySearchHandler.search(source, musicBot.getConfigManager().getConfig().getSpotifyClientId(), musicBot.getConfigManager().getConfig().getSpotifyClientSecret());
                            if(!trackList.isEmpty() && event.getGuild() != null && musicBot.getMusicManager().isConnected(event.getGuild())) {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":zzz:  Converting to youtube...")
                                        .setColor(Color.YELLOW);
                                event.getHook().editOriginal(" ").setEmbeds(embedBuilder.build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                boolean blacklisted = false;
                                int index = 0;
                                for(AudioTrack track : trackList) {
                                    if(!musicBot.getGMS().isBlacklisted(event.getGuild(), event.getMember(), track)) {
                                        musicBot.getMusicManager().add(event.getGuild(), track.getInfo().uri, ((index == 0) && startafterload));
                                    } else {
                                        blacklisted = true;
                                    }
                                    index++;
                                    try {
                                        TimeUnit.SECONDS.sleep(1);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if(blacklisted) {
                                    EmbedBuilder embedBuilder2 = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Successfully added " + trackList.size() + " songs from spotify (some tracks are blacklisted)")
                                            .setColor(Color.GREEN);
                                    event.getHook().editOriginal(" ").setEmbeds(embedBuilder2.build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                } else {
                                    EmbedBuilder embedBuilder2 = new EmbedBuilder()
                                            .setDescription(":white_check_mark:  Successfully added " + trackList.size() + " songs from spotify")
                                            .setColor(Color.GREEN);
                                    event.getHook().editOriginal(" ").setEmbeds(embedBuilder2.build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                }
                            } else {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setDescription(":warning:  Nothing was found")
                                        .setColor(Color.RED);
                                event.getHook().editOriginal(" ").setEmbeds(embedBuilder.build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            }
                        }
                    }).start();
                } else {
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setDescription(":warning:  Incompatible link")
                            .setColor(Color.RED);
                    event.getHook().editOriginal(" ").setEmbeds(embedBuilder.build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                }
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<AudioTrack> trackList = YTSearchHandler.search(source);
                        if(!trackList.isEmpty()) {
                            musicBot.getMusicManager().add(event.getGuild(), trackList.get(0).getInfo().uri, event, startafterload);
                        } else {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":warning:  Nothing was found")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    }
                }).start();
            }
        } else {
            if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
                if(!this.musicBot.getMusicManager().getQueue(event.getGuild()).isEmpty() || this.musicBot.getMusicManager().isPaused(event.getGuild()) || this.musicBot.getMusicManager().getPlayingTrack(event.getGuild()) != null) {
                    if(this.musicBot.getMusicManager().isPaused(event.getGuild())) {
                        this.musicBot.getMusicManager().setPause(event.getGuild(), false);
                    }
                    if(this.musicBot.getMusicManager().getPlayingTrack(event.getGuild()) == null) {
                        this.musicBot.getMusicManager().next(event.getGuild());
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
    private void playnow(SlashCommandInteractionEvent event) {
        if(event.getOption("song") != null) {
            String source = event.getOption("song").getAsString();
            if(source.startsWith("http://") || source.startsWith("https://")) {
                this.musicBot.getMusicManager().playnow(event.getGuild(), source, event);
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<AudioTrack> trackList = YTSearchHandler.search(source);
                        if(!trackList.isEmpty()) {
                            musicBot.getMusicManager().playnow(event.getGuild(), trackList.get(0).getInfo().uri, event);
                        } else {
                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setDescription(":warning:  Nothing was found")
                                    .setColor(Color.RED);
                            event.getHook().sendMessage("").addEmbeds(embedBuilder.build()).queue();
                        }
                    }
                }).start();
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

    public EmbedBuilder getHelpMessage() {
        return new EmbedBuilder()
                .setTitle("MusicBot Help")
                .setDescription("MusicBot by jandie1505")
                .addField("For users:", "/play <song name / link> - Add a specific song to queue\n" +
                        "/skip - Skipvote a specific song\n" +
                        "/nowplaying - Get the song that is currently playing\n" +
                        "/queue - Show the queue\n" +
                        "/queue <index> - Show the queue from a certain index (\"Queue pages\")\n" +
                        "/search <song name> - Search for a specific song and list the result\n", false)
                .addField("For DJs:", "/stop and /pause - Pause the player\n" +
                        "/play - Resume the player\n" +
                        "/leave - Disconnect the bot\n" +
                        "/forceskip - Skip a track\n" +
                        "/movetrack <from> <to> -Move a specific track in queue\n" +
                        "/remove <index> - Remove a specific song from queue\n" +
                        "/clear - Clear the queue\n" +
                        "/shuffle - Shuffles the queue\n" +
                        "/volume <0-200> - Change the volume\n" +
                        "/playnow <song name / link> - Plays a specific song immediately\n", false)
                .addField("For Admins:", "/mbsettings - Change settings of the bot", false);
    }
}
