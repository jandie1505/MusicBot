package net.jandie1505.musicbot.database;

import net.dv8tion.jda.api.entities.Guild;
import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.Console;
import org.json.JSONArray;
import org.sqlite.SQLiteErrorCode;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {

    private final MusicBot musicBot;
    private File databaseFile;
    private Connection connection;

    public DatabaseManager(MusicBot musicBot) throws IOException, SQLException, ClassNotFoundException {
        this.musicBot = musicBot;

        Class.forName("org.sqlite.JDBC");

        this.connect();

        createGuildsTable();
        createGuildWhitelistTable();
        createMusicBlacklistTable();

        MusicBot.LOGGER.info("Database successfully initialized");
    }

    private void connect() throws SQLException, IOException {
        databaseFile = new File(".", "database.sqlite");
        if(!databaseFile.exists()) {
            databaseFile.createNewFile();
        }

        String url = "jdbc:sqlite:" + databaseFile.getPath();
        connection = DriverManager.getConnection(url);

        MusicBot.LOGGER.info("Database successfully initialized");
    }

    // CREATE TABLES
    private void createGuildsTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS guilds (" +
                    "guildId INTEGER NOT NULL PRIMARY KEY," +
                    "DJRoles VARCHAR(1000) NOT NULL DEFAULT '[]'," +
                    "restrictToRoles INTEGER NOT NULL DEFAULT 0," +
                    "ephemeralState BOOLEAN NOT NULL DEFAULT true" +
                    ");";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();

            MusicBot.LOGGER.debug("Set up guilds table");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    private void createGuildWhitelistTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS guild_whitelist (" +
                    "guildId INTEGER NOT NULL PRIMARY KEY" +
                    ")";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            this.debugDatabaseLog("Set up table guild_whitelist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void createMusicBlacklistTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS music_blacklist (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "guildId INTEGER," +
                    "type INTEGER NOT NULL DEFAULT 0," +
                    "content VARCHAR(255)" +
                    ")";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            this.debugDatabaseLog("Set up table blacklist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    // GUILD MANAGEMENT

    public List<GuildData> getRegisteredGuilds() {
        List<GuildData> returnList = new ArrayList<>();

        try {
            String sql = "SELECT * FROM guilds;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                returnList.add(new GuildData(rs));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }


        return returnList;
    }

    public void updateGuild(GuildData guildData) {
        try {
            guildData.getStatement(connection).execute();
            this.debugDatabaseLog("Updated guild " + guildData.getGuildId());
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public GuildData getGuild(long guildId) {

        try {
            String sql = "SELECT * FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return new GuildData(rs);
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return new GuildData(guildId);
    }

    public void deleteGuild(long guildId) {
        try {
            String sql = "DELETE FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, guildId);
            statement.execute();
            this.debugDatabaseLog("Deleted guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void cleanupGuilds() {

        if (this.musicBot.getShardManager() == null) {
            return;
        }

        if (!this.musicBot.completeOnline()) {
            return;
        }

        this.debugDatabaseLog("Running database cleanup");

        for (GuildData guildData : this.getRegisteredGuilds()) {

            if (this.musicBot.getShardManager().getGuildById(guildData.getGuildId()) == null) {
                this.deleteGuild(guildData.getGuildId());
                continue;
            }

        }
    }

    // GUILD WHITELIST MANAGEMENT

    public void addGuildToWhitelist(long guildId) {
        try {
            String sql2 = "INSERT OR IGNORE INTO guild_whitelist (guildId)" +
                    " values (?);";
            PreparedStatement statement2 = connection.prepareStatement(sql2);
            statement2.setLong(1, guildId);
            statement2.execute();
            this.debugDatabaseLog("Added guild " + guildId + " to guild whitelist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void removeGuildFromWhitelist(long guildId) {
        try {
            String sql2 = "DELETE FROM guild_whitelist WHERE guildId = ?;";
            PreparedStatement statement2 = connection.prepareStatement(sql2);
            statement2.setLong(1, guildId);
            statement2.execute();
            this.debugDatabaseLog("Deleted guild " + guildId + " from guild whitelist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<Long> getGuildWhitelist() {
        List<Long> returnList = new ArrayList<>();

        try {
            String sql = "SELECT guildId FROM guild_whitelist;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getLong("guildId"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public boolean isGuildWhitelisted(long guildId) {
        try {
            String sql = "SELECT guildId FROM guild_whitelist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return true;
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
        return false;
    }

    // MUSIC BLACKLIST

    public void updateMusicBlacklistEntry(BlacklistEntry blacklistEntry) {
        try {
            blacklistEntry.getStatement(this.connection).execute();
            this.debugDatabaseLog("Added blacklist entry: " + blacklistEntry.getType() + " " + blacklistEntry.getGuildId() + " " + blacklistEntry.getContent());
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void deleteMusicBlacklistEntry(long id) {
        try {
            PreparedStatement statement = this.connection.prepareStatement(
                    "DELETE FROM music_blacklist WHERE id = ?;"
            );
            statement.setLong(1, id);
            statement.execute();
            this.debugDatabaseLog("Removed blacklist entry " + id);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<BlacklistEntry> getMusicBlacklist() {
        List<BlacklistEntry> returnList = new ArrayList<>();

        try {
            PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT * FROM music_blacklist;"
            );

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                returnList.add(new BlacklistEntry(rs));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public List<BlacklistEntry> getMusicBlacklist(long guildId) {
        List<BlacklistEntry> returnList = new ArrayList<>();

        try {
            PreparedStatement statement;

            if (guildId < 0) {
                statement = this.connection.prepareStatement(
                        "SELECT * FROM music_blacklist WHERE guildId IS NULL"
                );
            } else {
                statement = this.connection.prepareStatement(
                        "SELECT * FROM music_blacklist WHERE guildId = ?"
                );
            }

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                returnList.add(new BlacklistEntry(rs));
            }
        } catch (Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public BlacklistEntry getMusicBlacklistEntry(long id) {
        try {
            PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT * FROM music_blacklist WHERE id = ?"
            );

            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return new BlacklistEntry(rs);
            }
        } catch (Exception e) {
            this.logDatabaseError(e);
        }

        return new BlacklistEntry(id);
    }

    public void clearMusicBlacklist(long guildId) {
        try {
            PreparedStatement statement;

            if (guildId < 0) {
                statement = connection.prepareStatement(
                        "DELETE FROM music_blacklist WHERE guildId IS NULL;"
                );
            } else {
                statement = connection.prepareStatement(
                        "DELETE FROM music_blacklist WHERE guildId = ?"
                );
                statement.setLong(1, guildId);
            }

            statement.execute();
            this.debugDatabaseLog("Cleared blacklist of guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void cleanupMusicBlacklist() {

        if (this.musicBot.getShardManager() == null) {
            return;
        }

        for (BlacklistEntry blacklistEntry : this.getMusicBlacklist()) {

            if (blacklistEntry.getId() < 0) {
                continue;
            }

            Guild guild = this.musicBot.getShardManager().getGuildById(blacklistEntry.getGuildId());

            if (guild == null) {
                this.deleteMusicBlacklistEntry(blacklistEntry.getId());
                continue;
            }

        }

    }

    // Logger

    private void debugDatabaseLog(String message) {
        MusicBot.LOGGER.debug(message);
    }

    private void logDatabaseError(Exception e) {
        MusicBot.LOGGER.error("Database error", e);
    }
}
