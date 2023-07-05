package net.jandie1505.musicbot.database;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GuildData {
    private final long guildId;
    private final List<Long> djRoles;
    private int restrictToRoles;
    private boolean ephemeralState;

    public GuildData(long guildId) {
        this.guildId = guildId;
        this.djRoles = new ArrayList<>();
        this.restrictToRoles = 0;
        this.ephemeralState = true;
    }

    public GuildData(ResultSet rs) throws SQLException {
        this.guildId = rs.getLong("guildId");
        this.djRoles = new ArrayList<>();
        this.restrictToRoles = rs.getInt("restrictToRoles");
        this.ephemeralState = rs.getBoolean("ephemeralState");

        this.setDJRolesFromJSONArray(rs.getString("djRoles"));
    }

    public long getGuildId() {
        return guildId;
    }

    public List<Long> getDjRoles() {
        return djRoles;
    }

    public int getRestrictToRoles() {
        return restrictToRoles;
    }

    public void setRestrictToRoles(int restrictToRoles) {
        this.restrictToRoles = restrictToRoles;
    }

    public boolean isEphemeralState() {
        return ephemeralState;
    }

    public void setEphemeralState(boolean ephemeralState) {
        this.ephemeralState = ephemeralState;
    }

    protected PreparedStatement getStatement(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO guilds (guildId, DJRoles, restrictToRoles, ephemeralState) VALUES (?, ?, ?, ?);"
        );

        statement.setLong(1, this.guildId);
        statement.setString(2, new JSONArray(this.djRoles).toString());
        statement.setInt(3, this.restrictToRoles);
        statement.setBoolean(4, this.ephemeralState);

        return statement;
    }

    public void setDJRolesFromJSONArray(String jsonString) {

        JSONArray djRolesArray;

        try {
            djRolesArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            djRolesArray = new JSONArray();
        }

        List<Long> djRoles = new ArrayList<>();

        for (int i = 0; i < djRolesArray.length(); i++) {

            long roleId = djRolesArray.optLong(i, -1);

            if (roleId < 0) {
                continue;
            }

            djRoles.add(roleId);

        }

        this.djRoles.clear();
        this.djRoles.addAll(djRoles);
    }
}
