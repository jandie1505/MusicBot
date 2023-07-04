package net.jandie1505.musicbot.system;

import net.jandie1505.musicbot.MusicBot;
import net.jandie1505.musicbot.console.Console;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "guildId VARCHAR(255)," +
                    "DJRoles VARCHAR(255) DEFAULT '[]'," +
                    "restrictToRoles INTEGER DEFAULT 0," +
                    "ephemeralState BOOLEAN DEFAULT false," +
                    "jsonText VARCHAR(255) DEFAULT '{}'" +
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
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "guildId VARCHAR(255)" +
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
                    "guildId VARCHAR(255)," +
                    "link VARCHAR(255)" +
                    ")";
            String sql2 = "CREATE TABLE IF NOT EXISTS keyword_blacklist (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "guildId VARCHAR(255)," +
                    "keyword VARCHAR(255)" +
                    ")";
            String sql3 = "CREATE TABLE IF NOT EXISTS artist_blacklist (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "guildId VARCHAR(255)," +
                    "artist VARCHAR(255)" +
                    ")";
            PreparedStatement statement = connection.prepareStatement(sql);
            PreparedStatement statement2 = connection.prepareStatement(sql2);
            PreparedStatement statement3 = connection.prepareStatement(sql3);
            statement.execute();
            statement2.execute();
            statement3.execute();
            this.debugDatabaseLog("Set up table music_blacklist");
            this.debugDatabaseLog("Set up table keyword_blacklist");
            this.debugDatabaseLog("Set up table artist_blacklist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    // GUILD MANAGEMENT
    public List<String> getRegisteredGuilds() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT guildId FROM guilds;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                returnList.add(rs.getString("guildId"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }


        return returnList;
    }

    public void registerGuild(String guildId) {
        try {
            String sql = "SELECT guildId FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO guilds (guildId)" +
                        " values (?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.execute();
            }
            this.debugDatabaseLog("Registered guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void deleteGuild(String guildId) {
        try {
            String sql = "DELETE FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.execute();

            this.debugDatabaseLog("Deleted guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public String getDJRoles(String guildId) {
        String returnString = "";

        try {
            String sql = "SELECT DJRoles FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                returnString = rs.getString("DJRoles");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnString;
    }

    public void setDJRoles(String guildId, String DJRoles) {
        try {
            String sql = "UPDATE guilds SET DJRoles = ? WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, DJRoles);
            statement.setString(2, guildId);
            statement.execute();
            this.debugDatabaseLog("Updated moderatorRoles on guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public int getRestrictToRoles(String guildId) {
        try {
            String sql = "SELECT restrictToRoles FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getInt("restrictToRoles");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
        return 0;
    }

    public void setRestrictToRoles(String guildId, int state) {
        try {
            String sql = "UPDATE guilds SET restrictToRoles = ? WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, state);
            statement.setString(2, guildId);
            statement.execute();
            this.debugDatabaseLog("Updated restrictToRoles on guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public boolean getEphemeralState(String guildId) {
        try {
            String sql = "SELECT ephemeralState FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getBoolean("ephemeralState");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
        return false;
    }

    public void setEphemeralState(String guildId, boolean state) {
        try {
            String sql = "UPDATE guilds SET ephemeralState = ? WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBoolean(1, state);
            statement.setString(2, guildId);
            statement.execute();
            this.debugDatabaseLog("Updated ephemeralState on guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    // GUILD WHITELIST MANAGEMENT
    public void addGuildToWhitelist(String guildId) {
        try {
            String sql = "SELECT guildId FROM guild_whitelist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO guild_whitelist (guildId)" +
                        " values (?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.execute();
                this.debugDatabaseLog("Added guild " + guildId + " to guild whitelist");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void removeGuildFromWhitelist(String guildId) {
        try {
            String sql = "SELECT guildId FROM guild_whitelist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM guild_whitelist (guildId) WHERE guildId = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.execute();
                this.debugDatabaseLog("Deleted guild " + guildId + " from guild whitelist");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<String> getGuildWhitelist() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT guildId FROM guild_whitelist;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("guildId"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public void clearGuildWhitelist() {
        try {
            String sql = "DELETE FROM guild_whitelist;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            this.debugDatabaseLog("Cleared guild_whitelist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public boolean isGuildWhitelisted(String guildId) {
        try {
            String sql = "SELECT guildId FROM guild_whitelist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return true;
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
        return false;
    }

    // GLOBAL MUSIC BLACKLIST
    public void addToGlobalBlacklist(String link) {
        try {
            String sql = "SELECT link FROM music_blacklist WHERE guildId IS NULL AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, link);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO music_blacklist (guildId, link)" +
                        " values (NULL, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, link);
                statement2.execute();
                this.debugDatabaseLog("Added link to global blacklist");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void deleteFromGlobalBlacklist(String link) {
        try {
            String sql = "SELECT link FROM music_blacklist WHERE guildId IS NULL AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, link);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM music_blacklist WHERE guildId IS NULL AND link = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, link);
                statement2.execute();
                this.debugDatabaseLog("Removed link from global blacklist");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<String> getGlobalBlacklist() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT link FROM music_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("link"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public void clearGlobalBlacklist() {
        try {
            String sql = "DELETE FROM music_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            this.debugDatabaseLog("Cleared global blacklist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    // GUILD MUSIC BLACKLIST
    public void addToBlacklist(String guildId, String link) {
        try {
            String sql = "SELECT link FROM music_blacklist WHERE guildId = ? AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, link);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO music_blacklist (guildId, link)" +
                        " values (?, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, link);
                statement2.execute();
                this.debugDatabaseLog("Added link to blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void deleteFromBlacklist(String guildId, String link) {
        try {
            String sql = "SELECT link FROM music_blacklist WHERE guildId = ? AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, link);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM music_blacklist WHERE guildId = ? AND link = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, link);
                statement2.execute();
                this.debugDatabaseLog("Removed link from blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<String> getBlacklist(String guildId) {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT link FROM music_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("link"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public void clearBlacklist(String guildId) {
        try {
            String sql = "DELETE FROM music_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.execute();
            this.debugDatabaseLog("Cleared blacklist of guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    // GLOBAL KEYWORD BLACKLIST
    public void addToGlobalKeywordBlacklist(String keyword) {
        try {
            String sql = "SELECT keyword FROM keyword_blacklist WHERE guildId IS NULL AND keyword = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, keyword);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO keyword_blacklist (guildId, keyword)" +
                        " values (NULL, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, keyword);
                statement2.execute();
                this.debugDatabaseLog("Added keyword to global keyword blacklist");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void deleteFromGlobalKeywordBlacklist(String keyword) {
        try {
            String sql = "SELECT keyword FROM keyword_blacklist WHERE guildId IS NULL AND keyword = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, keyword);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM keyword_blacklist WHERE guildId IS NULL AND keyword = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, keyword);
                statement2.execute();
                this.debugDatabaseLog("Removed keyword from global keyword blacklist");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<String> getGlobalKeywordBlacklist() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT keyword FROM keyword_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("keyword"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public void clearGlobalKeywordBlacklist() {
        try {
            String sql = "DELETE FROM keyword_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            this.debugDatabaseLog("Cleared global keyword blacklist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    // GUILD KEYWORD BLACKLIST
    public void addToKeywordBlacklist(String guildId, String keyword) {
        try {
            String sql = "SELECT keyword FROM keyword_blacklist WHERE guildId = ? AND keyword = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, keyword);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO keyword_blacklist (guildId, keyword)" +
                        " values (?, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, keyword);
                statement2.execute();
                this.debugDatabaseLog("Added keyword to keyword blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void deleteFromKeywordBlacklist(String guildId, String keyword) {
        try {
            String sql = "SELECT keyword FROM keyword_blacklist WHERE guildId = ? AND keyword = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, keyword);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM keyword_blacklist WHERE guildId = ? AND keyword = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, keyword);
                statement2.execute();
                this.debugDatabaseLog("Removed keyword from keyword blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<String> getKeywordBlacklist(String guildId) {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT keyword FROM keyword_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("keyword"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public void clearKeywordBlacklist(String guildId) {
        try {
            String sql = "DELETE FROM keyword_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.execute();
            this.debugDatabaseLog("Cleared keyword blacklist of guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    // GLOBAL ARTIST BLACKLIST
    public void addToGlobalArtistBlacklist(String artist) {
        try {
            String sql = "SELECT artist FROM artist_blacklist WHERE guildId IS NULL AND artist = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, artist);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO artist_blacklist (guildId, artist)" +
                        " values (NULL, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, artist);
                statement2.execute();
                this.debugDatabaseLog("Added artist to global artist blacklist");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void deleteFromGlobalArtistBlacklist(String artist) {
        try {
            String sql = "SELECT artist FROM artist_blacklist WHERE guildId IS NULL AND artist = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, artist);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM artist_blacklist WHERE guildId IS NULL AND artist = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, artist);
                statement2.execute();
                this.debugDatabaseLog("Removed artist from global artist blacklist");
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<String> getGlobalArtistBlacklist() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT artist FROM artist_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("artist"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public void clearGlobalArtistBlacklist() {
        try {
            String sql = "DELETE FROM artist_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            this.debugDatabaseLog("Cleared global artist blacklist");
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    // GUILD ARTIST BLACKLIST
    public void addToArtistBlacklist(String guildId, String artist) {
        try {
            String sql = "SELECT artist FROM artist_blacklist WHERE guildId = ? AND artist = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, artist);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO artist_blacklist (guildId, artist)" +
                        " values (?, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, artist);
                statement2.execute();
                this.debugDatabaseLog("Added artist to artist blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public void deleteFromArtistBlacklist(String guildId, String artist) {
        try {
            String sql = "SELECT artist FROM artist_blacklist WHERE guildId = ? AND artist = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, artist);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM artist_blacklist WHERE guildId = ? AND artist = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, artist);
                statement2.execute();
                this.debugDatabaseLog("Removed artist from artist blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    public List<String> getArtistBlacklist(String guildId) {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT artist FROM artist_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("artist"));
            }
        } catch(Exception e) {
            this.logDatabaseError(e);
        }

        return returnList;
    }

    public void clearArtistBlacklist(String guildId) {
        try {
            String sql = "DELETE FROM artist_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.execute();
            this.debugDatabaseLog("Cleared artist blacklist of guild " + guildId);
        } catch(Exception e) {
            this.logDatabaseError(e);
        }
    }

    private void debugDatabaseLog(String message) {
        MusicBot.LOGGER.debug(message);
    }

    private void logDatabaseError(Exception e) {
        MusicBot.LOGGER.error("Database error", e);
    }
}
