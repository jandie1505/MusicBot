package net.jandie1505.musicbot.slashcommands;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.slashcommandapi.interfaces.SlashCommandPermissionRequest;

public class UserPermissionRequest implements SlashCommandPermissionRequest {

    private final MusicBot musicBot;

    public UserPermissionRequest(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public boolean hasPermission(SlashCommandInteraction interaction) {
        return musicBot.getGMS().memberHasUserPermissions(interaction.getMember());
    }
}
