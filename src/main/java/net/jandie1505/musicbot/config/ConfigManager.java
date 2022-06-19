package net.jandie1505.musicbot.config;

import net.jandie1505.musicbot.MusicBot;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class ConfigManager {

    private final MusicBot musicBot;
    private final BotConfig config;

    public ConfigManager(MusicBot musicBot) {
        this.musicBot = musicBot;
        this.config = new BotConfig();
    }

    public BotConfig getConfig() {
        return this.config;
    }

    public void loadConfig(File file) throws IOException, JSONException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }

        String out = sb.toString();

        JSONObject jsonConfig = new JSONObject(out);

        try {
            this.config.setToken(jsonConfig.getString("token"));
        } catch (JSONException | IllegalArgumentException e) {

        }

        try {
            this.config.setShardsCount(jsonConfig.getInt("shardsCount"));
        } catch (JSONException | IllegalArgumentException e) {

        }

        try {
            this.config.setPublicMode(jsonConfig.getBoolean("publicMode"));
        } catch (JSONException | IllegalArgumentException e) {

        }

        try {
            this.config.setBotOwner(jsonConfig.getString("botOwner"));
        } catch (JSONException | IllegalArgumentException e) {

        }

        try {
            this.config.setSpotifyClientId(jsonConfig.getString("spotifyClientId"));
        } catch (JSONException | IllegalArgumentException e) {

        }

        try {
            this.config.setSpotifyClientSecret(jsonConfig.getString("spotifyClientSecret"));
        } catch (JSONException | IllegalArgumentException e) {

        }
    }

    public void saveConfig(File file) throws IOException {
        JSONObject jsonConfig = new JSONObject();

        jsonConfig.put("token", this.config.getToken());
        jsonConfig.put("shardsCount", this.config.getShardsCount());
        jsonConfig.put("publicMode", this.config.isPublicMode());
        jsonConfig.put("botOwner", this.config.getBotOwner());
        jsonConfig.put("spotifyClientId", this.config.getSpotifyClientId());
        jsonConfig.put("spotifyClientSecret", this.config.getSpotifyClientSecret());

        FileWriter writer = new FileWriter(file);
        writer.write(jsonConfig.toString(4));
        writer.flush();
        writer.close();
    }
}
