package net.jandie1505.musicbot.system;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;

public class EventsButtons extends ListenerAdapter {
    @Override
    public void onButtonClick(ButtonClickEvent event) {
        if(event.getButton().getId().equalsIgnoreCase("playbutton")) {
            if(GMS.memberHasDJPermissions(event.getMember())) {
                if(event.getGuild() != null) {
                    Message m = event.getMessage();
                    if(m != null) {
                        if(MusicManager.getPlayingTrack(event.getGuild()) == null) {
                            MusicManager.next(event.getGuild());
                        }
                        if(MusicManager.isPaused(event.getGuild())) {
                            MusicManager.setPause(event.getGuild(), false);
                        }
                        event.editMessage(Messages.nowplayingMessage(event.getGuild(), GMS.memberHasDJPermissions(event.getMember())).build()).queue();
                    }
                }
            }
        } else if(event.getButton().getId().equalsIgnoreCase("pausebutton")) {
            if(GMS.memberHasDJPermissions(event.getMember())) {
                if(event.getGuild() != null) {
                    Message m = event.getMessage();
                    if(m != null) {
                        if(!MusicManager.isPaused(event.getGuild())) {
                            MusicManager.setPause(event.getGuild(), true);
                        }
                        event.editMessage(Messages.nowplayingMessage(event.getGuild(), GMS.memberHasDJPermissions(event.getMember())).build()).queue();
                    }
                }
            }
        } else if(event.getButton().getId().equalsIgnoreCase("refreshbutton")) {
            if(event.getGuild() != null) {
                Message m = event.getMessage();
                if(m != null) {
                    event.editMessage(Messages.nowplayingMessage(event.getGuild(), GMS.memberHasDJPermissions(event.getMember())).build()).queue();
                }
            }
        } else if(event.getButton().getId().equalsIgnoreCase("nowplayingskipbutton")) {
            if(event.getGuild() != null) {
                Message m = event.getMessage();
                if(m != null) {
                    MusicManager.next(event.getGuild());
                    event.editMessage(Messages.nowplayingMessage(event.getGuild(), GMS.memberHasDJPermissions(event.getMember())).build()).queue();
                }
            }
        }
    }
}
