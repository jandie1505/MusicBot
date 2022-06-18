package net.jandie1505.musicbot.eventlisteners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.utilities.Messages;

public class EventsButtons extends ListenerAdapter {

    private final MusicBot musicBot;

    public EventsButtons(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(event.getButton().getId().equalsIgnoreCase("playbutton")) {
            if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
                if(event.getGuild() != null) {
                    Message m = event.getMessage();
                    if(m != null) {
                        if(this.musicBot.getMusicManager().getPlayingTrack(event.getGuild()) == null) {
                            this.musicBot.getMusicManager().next(event.getGuild());
                        }
                        if(this.musicBot.getMusicManager().isPaused(event.getGuild())) {
                            this.musicBot.getMusicManager().setPause(event.getGuild(), false);
                        }
                        event.editMessage(Messages.nowplayingMessage(musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build()).queue();
                    }
                }
            }
        } else if(event.getButton().getId().equalsIgnoreCase("pausebutton")) {
            if(this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
                if(event.getGuild() != null) {
                    Message m = event.getMessage();
                    if(m != null) {
                        if(!this.musicBot.getMusicManager().isPaused(event.getGuild())) {
                            this.musicBot.getMusicManager().setPause(event.getGuild(), true);
                        }
                        event.editMessage(Messages.nowplayingMessage(musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build()).queue();
                    }
                }
            }
        } else if(event.getButton().getId().equalsIgnoreCase("refreshbutton")) {
            if(event.getGuild() != null) {
                Message m = event.getMessage();
                if(m != null) {
                    event.editMessage(Messages.nowplayingMessage(musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build()).queue();
                }
            }
        } else if(event.getButton().getId().equalsIgnoreCase("nowplayingskipbutton")) {
            if(event.getGuild() != null) {
                Message m = event.getMessage();
                if(m != null) {
                    this.musicBot.getMusicManager().next(event.getGuild());
                    event.editMessage(Messages.nowplayingMessage(musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build()).queue();
                }
            }
        }
    }
}
