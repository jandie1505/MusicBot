package net.jandie1505.musicbot.config;

public class BotConfig {

    private String token;
    private int shardsCount;
    private boolean publicMode;
    private String botOwner;
    private boolean disableShardsCheck;

    public BotConfig() {
        this.token = "";
        this.shardsCount = 1;
        this.publicMode = false;
        this.botOwner = "";
        this.disableShardsCheck = false;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getShardsCount() {
        return shardsCount;
    }

    public void setShardsCount(int shardsCount) {
        if(shardsCount > 1) {
            this.shardsCount = shardsCount;
        } else {
            throw new IllegalArgumentException("shardsCount must be higher than 1");
        }
    }

    public boolean isPublicMode() {
        return publicMode;
    }

    public void setPublicMode(boolean publicMode) {
        this.publicMode = publicMode;
    }

    public String getBotOwner() {
        return botOwner;
    }

    public void setBotOwner(String botOwner) {
        this.botOwner = botOwner;
    }

    public boolean isDisableShardsCheck() {
        return disableShardsCheck;
    }

    public void setDisableShardsCheck(boolean disableShardsCheck) {
        this.disableShardsCheck = disableShardsCheck;
    }
}
