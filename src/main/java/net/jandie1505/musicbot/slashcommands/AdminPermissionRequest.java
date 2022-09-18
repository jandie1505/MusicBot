package net.jandie1505.musicbot.slashcommands;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.slashcommandapi.interfaces.SlashCommandPermissionRequest;

public class AdminPermissionRequest implements SlashCommandPermissionRequest {

    private final MusicBot musicBot;

    public AdminPermissionRequest(MusicBot musicBot) {
        this.musicBot = musicBot;
    }

    @Override
    public boolean hasPermission(SlashCommandInteraction interaction) {
        if(interaction.getMember() != null) {
            return musicBot.getGMS().memberHasAdminPermissions(interaction.getMember());
        } else {
            return false;
        }
    }
}
