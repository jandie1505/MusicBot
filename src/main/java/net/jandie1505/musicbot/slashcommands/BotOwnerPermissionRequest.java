package net.jandie1505.musicbot.slashcommands;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.slashcommandapi.interfaces.SlashCommandPermissionRequest;

public class BotOwnerPermissionRequest implements SlashCommandPermissionRequest {

    private final MusicBot musicBot;

    public BotOwnerPermissionRequest(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public boolean hasPermission(SlashCommandInteraction interaction) {
        if(interaction.getUser().getId().equals(musicBot.getConfigManager().getConfig().getBotOwner())) {
            return true;
        }
        return false;
    }
}
