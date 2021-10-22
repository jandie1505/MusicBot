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
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY," +
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
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY," +
                    "guildId VARCHAR(255)" +
                    ")";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            Console.messageDB("Set up table guild_whitelist");
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
            String sql = "SELECT restrictToRoles FROM guilds WHERE guildId = ?";
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
            String sql = "SELECT ephemeralState FROM guilds WHERE guildId = ?";
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
            String sql = "SELECT guildId FROM guild_whitelist WHERE guildId = ?";
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
            String sql = "SELECT guildId FROM guild_whitelist WHERE guildId = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                String sql2 = "DELETE FROM guild_whitelist (guildId) WHERE guildId = ?";
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
            String sql = "SELECT guildId FROM guild_whitelist";
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
            String sql = "DELETE FROM guild_whitelist";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            Console.messageDB("Cleared guild_whitelist");
        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static boolean isGuildWhitelisted(String guildId) {
        try {
            String sql = "SELECT guildId FROM guild_whitelist WHERE guildId = ?";
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
}
