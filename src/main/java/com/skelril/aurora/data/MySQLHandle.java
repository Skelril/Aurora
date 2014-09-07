/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.aurora.data;

import java.sql.*;

public class MySQLHandle {
    private static String database = "";
    private static String username = "";
    private static String password = "";

    public static void setDatabase(String database) {
        MySQLHandle.database = database;
    }

    public static void setUsername(String username) {
        MySQLHandle.username = username;
    }

    public static void setPassword(String password) {
        MySQLHandle.password = password;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(database, username, password);
    }

    public static int getPlayerId(String name) throws SQLException {
        try (Connection connection = getConnection()) {
            String sql = "SELECT playerid FROM `lb-players` WHERE playername = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                ResultSet results = statement.executeQuery();
                if (results.next()) return results.getInt(1);
            }
        }
        return -1;
    }

    public static String getPlayerName(int id) throws SQLException {
        try (Connection connection = getConnection()) {
            String sql = "SELECT playername FROM `lb-players` WHERE playerid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                ResultSet results = statement.executeQuery();
                if (results.next()) return results.getString(1);
            }
        }
        return null;
    }
}
