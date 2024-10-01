package net.jandie1505.musicbot.eventlisteners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.music.MusicPlayer;
import net.jandie1505.musicbot.utilities.Messages;

public class EventsButtons extends ListenerAdapter {

    private final MusicBot musicBot;

    public EventsButtons(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        if(event.getButton().getId().equalsIgnoreCase("nowplaying_button_play")) {

            if (!this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
                return;
            }

            if (event.getGuild() == null) {
                return;
            }

            MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(event.getGuild().getIdLong());

            if (player.isPaused()) {
                player.setPause(false);
            }

            if (player.getPlayingTrack() == null && !player.getQueue().isEmpty()) {
                player.nextTrack();
            }

            event.editMessage(MessageEditData.fromCreateData(Messages.nowplayingMessage(this.musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build())).queue();

        } else if(event.getButton().getId().equalsIgnoreCase("nowplaying_button_pause")) {

            if (!this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
                return;
            }

            if (event.getGuild() == null) {
                return;
            }

            MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(event.getGuild().getIdLong());

            player.setPause(true);

            event.editMessage(MessageEditData.fromCreateData(Messages.nowplayingMessage(this.musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build())).queue();

        } else if(event.getButton().getId().equalsIgnoreCase("nowplaying_button_refresh")) {

            if (event.getGuild() == null) {
                return;
            }

            event.editMessage(MessageEditData.fromCreateData(Messages.nowplayingMessage(this.musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build())).queue();

        } else if(event.getButton().getId().equalsIgnoreCase("nowplaying_button_skip")) {

            if (!this.musicBot.getGMS().memberHasDJPermissions(event.getMember())) {
                return;
            }

            if (event.getGuild() == null) {
                return;
            }

            MusicPlayer player = this.musicBot.getMusicManager().getMusicPlayer(event.getGuild().getIdLong());

            player.nextTrack();

            event.editMessage(MessageEditData.fromCreateData(Messages.nowplayingMessage(this.musicBot, event.getGuild(), this.musicBot.getGMS().memberHasDJPermissions(event.getMember())).build())).queue();

        }

    }
}
