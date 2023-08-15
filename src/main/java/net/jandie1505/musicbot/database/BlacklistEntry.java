package net.jandie1505.musicbot.database;

import java.sql.*;

public class BlacklistEntry {
    private final long id;
    private long guildId;
    private int type;
    private String content;

    public BlacklistEntry(long id) {
        this.id = id;
        this.guildId = -1;
        this.type = 0;
        this.content = "";
    }

    public BlacklistEntry(ResultSet rs) throws SQLException {
        this.id = rs.getLong("id");
        this.guildId = rs.getLong("guildId");
        this.type = rs.getInt("type");
        this.content = rs.getString("content");
    }

    public long getId() {
        return id;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PreparedStatement getStatement(Connection connection) throws SQLException {
        PreparedStatement statement;

        if (this.id < 0) {
            statement = connection.prepareStatement(
                    "INSERT OR REPLACE INTO music_blacklist (guildId, type, content) VALUES (?, ?, ?)"
            );
        } else {
            statement = connection.prepareStatement(
                    "INSERT OR REPLACE INTO music_blacklist (guildId, type, content, id) VALUES (?, ?, ?, ?)"
            );
        }

        if (this.guildId < 0) {
            statement.setNull(1, Types.INTEGER);
        } else {
            statement.setLong(1, this.guildId);
        }

        statement.setInt(2, this.type);
        statement.setString(3, this.content);

        if (this.id >= 0) {
            statement.setLong(4, this.id);
        }

        return statement;
    }
}
