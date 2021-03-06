package net.jandie1505.musicbot.system;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jandie1505.musicbot.console.Console;

import static net.jandie1505.musicbot.MusicBot.upsertCommands;

public class EventsBasic extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent event) {
        upsertCommands(false);
        Console.messageShardManager("Shard " + event.getJDA().getShardInfo().getShardId() + " ready");
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        Console.messageShardManager("Shard " + event.getJDA().getShardInfo().getShardId() + " stopped");
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        GMS.setupGuild(event.getGuild());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        GMS.leaveGuild(event.getGuild().getId());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if(event.getMember() == event.getGuild().getSelfMember()) {
            MusicManager.disconnect(event.getGuild());
        }
    }
}
