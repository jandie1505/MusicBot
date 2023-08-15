package net.jandie1505.musicbot.eventlisteners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.Console;

public class EventsBasic extends ListenerAdapter {

    private final MusicBot musicBot;

    public EventsBasic(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public void onReady(ReadyEvent event) {
        this.musicBot.upsertCommands();
        MusicBot.LOGGER.info("Shard " + event.getJDA().getShardInfo().getShardId() + " ready");
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        MusicBot.LOGGER.info("Shard " + event.getJDA().getShardInfo().getShardId() + " stopped");
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        this.musicBot.getGMS().setupGuild(event.getGuild());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        this.musicBot.getDatabaseManager().deleteGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {

        if (event.getChannelLeft() == null) {
            return;
        }

        if(event.getMember() == event.getGuild().getSelfMember()) {
            this.musicBot.getMusicManager().disconnect(event.getGuild());
        }
    }
}
