package net.jandie1505.musicbot.system;

import net.jandie1505.musicbot.console.Console;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static File databaseFile;
    private static Connection connection;

    // INIT
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        connect();

        createGuildsTable();
        createGuildWhitelistTable();
        createMusicBlacklistTable();
        Console.messageDB("Database successfully initialized");
    }

    private static void connect() throws SQLException, IOException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        databaseFile = new File(".", "database.sqlite");
        if(!databaseFile.exists()) {
            databaseFile.createNewFile();
        }

        String url = "jdbc:sqlite:" + databaseFile.getPath();
        connection = DriverManager.getConnection(url);
        Console.messageDB("Connected to database");
    }

    // CREATE TABLES
    private static void createGuildsTable() {
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
            Console.messageDB("Set up table guilds");
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void createGuildWhitelistTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS guild_whitelist (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "guildId VARCHAR(255)" +
                    ")";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            Console.messageDB("Set up table guild_whitelist");
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void createMusicBlacklistTable() {
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
            Console.messageDB("Set up table music_blacklist");
            Console.messageDB("Set up table keyword_blacklist");
            Console.messageDB("Set up table artist_blacklist");
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // GUILD MANAGEMENT
    public static List<String> getRegisteredGuilds() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT guildId FROM guilds;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                returnList.add(rs.getString("guildId"));
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }


        return returnList;
    }

    public static void registerGuild(String guildId) {
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
            Console.messageDB("Registered guild " + guildId);
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void deleteGuild(String guildId) {
        try {
            String sql = "DELETE FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.execute();

            Console.messageDB("Deleted guild " + guildId);
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static String getDJRoles(String guildId) {
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
            System.out.println("Database error: " + e.getMessage());
        }

        return returnString;
    }

    public static void setDJRoles(String guildId, String DJRoles) {
        try {
            String sql = "UPDATE guilds SET DJRoles = ? WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, DJRoles);
            statement.setString(2, guildId);
            statement.execute();
            Console.messageDB("Updated moderatorRoles on guild " + guildId);
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static int getRestrictToRoles(String guildId) {
        try {
            String sql = "SELECT restrictToRoles FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getInt("restrictToRoles");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return 0;
    }

    public static void setRestrictToRoles(String guildId, int state) {
        try {
            String sql = "UPDATE guilds SET restrictToRoles = ? WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, state);
            statement.setString(2, guildId);
            statement.execute();
            Console.messageDB("Updated restrictToRoles on guild " + guildId);
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static boolean getEphemeralState(String guildId) {
        try {
            String sql = "SELECT ephemeralState FROM guilds WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getBoolean("ephemeralState");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return false;
    }

    public static void setEphemeralState(String guildId, boolean state) {
        try {
            String sql = "UPDATE guilds SET ephemeralState = ? WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBoolean(1, state);
            statement.setString(2, guildId);
            statement.execute();
            Console.messageDB("Updated ephemeralState on guild " + guildId);
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // GUILD WHITELIST MANAGEMENT
    public static void addGuildToWhitelist(String guildId) {
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
                Console.messageDB("Added guild " + guildId + " to guild whitelist");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void removeGuildFromWhitelist(String guildId) {
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
                Console.messageDB("Deleted guild " + guildId + " from guild whitelist");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static List<String> getGuildWhitelist() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT guildId FROM guild_whitelist;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("guildId"));
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return returnList;
    }

    public static void clearGuildWhitelist() {
        try {
            String sql = "DELETE FROM guild_whitelist;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            Console.messageDB("Cleared guild_whitelist");
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static boolean isGuildWhitelisted(String guildId) {
        try {
            String sql = "SELECT guildId FROM guild_whitelist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return true;
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return false;
    }

    // GLOBAL MUSIC BLACKLIST
    public static void addToGlobalBlacklist(String link) {
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
                Console.messageDB("Added link to global blacklist");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void deleteFromGlobalBlacklist(String link) {
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
                Console.messageDB("Removed link from global blacklist");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static List<String> getGlobalBlacklist() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT link FROM music_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("link"));
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return returnList;
    }

    public static void clearGlobalBlacklist() {
        try {
            String sql = "DELETE FROM music_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            Console.messageDB("Cleared global blacklist");
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // GUILD MUSIC BLACKLIST
    public static void addToBlacklist(String guildId, String link) {
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
                Console.messageDB("Added link to blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void deleteFromBlacklist(String guildId, String link) {
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
                Console.messageDB("Removed link from blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static List<String> getBlacklist(String guildId) {
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
            System.out.println("Database error: " + e.getMessage());
        }

        return returnList;
    }

    public static void clearBlacklist(String guildId) {
        try {
            String sql = "DELETE FROM music_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.execute();
            Console.messageDB("Cleared blacklist of guild " + guildId);
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // GLOBAL KEYWORD BLACKLIST
    public static void addToGlobalKeywordBlacklist(String link) {
        try {
            String sql = "SELECT link FROM keyword_blacklist WHERE guildId IS NULL AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, link);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO keyword_blacklist (guildId, link)" +
                        " values (NULL, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, link);
                statement2.execute();
                Console.messageDB("Added link to global keyword blacklist");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void deleteFromGlobalKeywordBlacklist(String link) {
        try {
            String sql = "SELECT link FROM keyword_blacklist WHERE guildId IS NULL AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, link);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM keyword_blacklist WHERE guildId IS NULL AND link = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, link);
                statement2.execute();
                Console.messageDB("Removed link from global keyword blacklist");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static List<String> getGlobalKeywordBlacklist() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT link FROM keyword_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("link"));
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return returnList;
    }

    public static void clearGlobalKeywordBlacklist() {
        try {
            String sql = "DELETE FROM keyword_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            Console.messageDB("Cleared global keyword blacklist");
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // GUILD KEYWORD BLACKLIST
    public static void addToKeywordBlacklist(String guildId, String link) {
        try {
            String sql = "SELECT link FROM keyword_blacklist WHERE guildId = ? AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, link);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO keyword_blacklist (guildId, link)" +
                        " values (?, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, link);
                statement2.execute();
                Console.messageDB("Added link to keyword blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void deleteFromKeywordBlacklist(String guildId, String link) {
        try {
            String sql = "SELECT link FROM keyword_blacklist WHERE guildId = ? AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, link);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM keyword_blacklist WHERE guildId = ? AND link = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, link);
                statement2.execute();
                Console.messageDB("Removed link from keyword blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static List<String> getKeywordBlacklist(String guildId) {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT link FROM keyword_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("link"));
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return returnList;
    }

    public static void clearKeywordBlacklist(String guildId) {
        try {
            String sql = "DELETE FROM keyword_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.execute();
            Console.messageDB("Cleared keyword blacklist of guild " + guildId);
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // GLOBAL ARTIST BLACKLIST
    public static void addToGlobalArtistBlacklist(String link) {
        try {
            String sql = "SELECT link FROM artist_blacklist WHERE guildId IS NULL AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, link);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO artist_blacklist (guildId, link)" +
                        " values (NULL, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, link);
                statement2.execute();
                Console.messageDB("Added link to global artist blacklist");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void deleteFromGlobalArtistBlacklist(String link) {
        try {
            String sql = "SELECT link FROM artist_blacklist WHERE guildId IS NULL AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, link);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM artist_blacklist WHERE guildId IS NULL AND link = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, link);
                statement2.execute();
                Console.messageDB("Removed link from global artist blacklist");
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static List<String> getGlobalArtistBlacklist() {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT link FROM artist_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("link"));
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return returnList;
    }

    public static void clearGlobalArtistBlacklist() {
        try {
            String sql = "DELETE FROM artist_blacklist WHERE guildId IS NULL;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            Console.messageDB("Cleared global artist blacklist");
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // GUILD ARTIST BLACKLIST
    public static void addToArtistBlacklist(String guildId, String link) {
        try {
            String sql = "SELECT link FROM artist_blacklist WHERE guildId = ? AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, link);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()) {
                String sql2 = "INSERT INTO artist_blacklist (guildId, link)" +
                        " values (?, ?);";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, link);
                statement2.execute();
                Console.messageDB("Added link to artist blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void deleteFromArtistBlacklist(String guildId, String link) {
        try {
            String sql = "SELECT link FROM artist_blacklist WHERE guildId = ? AND link = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.setString(2, link);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM artist_blacklist WHERE guildId = ? AND link = ?;";
                PreparedStatement statement2 = connection.prepareStatement(sql2);
                statement2.setString(1, guildId);
                statement2.setString(2, link);
                statement2.execute();
                Console.messageDB("Removed link from artist blacklist of guild " + guildId);
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static List<String> getArtistBlacklist(String guildId) {
        List<String> returnList = new ArrayList<>();

        try {
            String sql = "SELECT link FROM artist_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                returnList.add(rs.getString("link"));
            }
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return returnList;
    }

    public static void clearArtistBlacklist(String guildId) {
        try {
            String sql = "DELETE FROM artist_blacklist WHERE guildId = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            statement.execute();
            Console.messageDB("Cleared artist blacklist of guild " + guildId);
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
