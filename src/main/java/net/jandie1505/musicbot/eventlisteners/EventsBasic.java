package net.jandie1505.musicbot.eventlisteners;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
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
        this.musicBot.upsertCommands(false);
        Console.messageShardManager("Shard " + event.getJDA().getShardInfo().getShardId() + " ready");
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        Console.messageShardManager("Shard " + event.getJDA().getShardInfo().getShardId() + " stopped");
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        this.musicBot.getGMS().setupGuild(event.getGuild());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        this.musicBot.getGMS().leaveGuild(event.getGuild().getId());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if(event.getMember() == event.getGuild().getSelfMember()) {
            this.musicBot.getMusicManager().disconnect(event.getGuild());
        }
    }
}
