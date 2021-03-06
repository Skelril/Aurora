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

package com.skelril.aurora.economic.store.mysql;

import com.skelril.aurora.data.MySQLHandle;
import com.skelril.aurora.data.MySQLPreparedStatement;
import com.skelril.aurora.economic.store.ItemPricePair;
import com.skelril.aurora.economic.store.ItemStoreDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MySQLItemStoreDatabase implements ItemStoreDatabase {
    private static final String columns = "`name`, `price`, `buyable`, `sellable`";
    private Queue<MySQLPreparedStatement> queue = new LinkedList<>();

    @Override
    public boolean load() {
        try (Connection connection = MySQLHandle.getConnection()) {
            String mainSQL = "CREATE TABLE IF NOT EXISTS `market-items` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`name` VARCHAR(50) NOT NULL," +
                    "`price` DOUBLE NOT NULL," +
                    "`buyable` TINYINT(1) NOT NULL," +
                    "`sellable` TINYINT(1) NOT NULL," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE INDEX `name` (`name`)" +
                    ") ENGINE=MyISAM;";
            try (PreparedStatement statement = connection.prepareStatement(mainSQL)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean save() {
        if (queue.isEmpty()) return true;
        try (Connection connection = MySQLHandle.getConnection()) {
            connection.setAutoCommit(false);
            while (!queue.isEmpty()) {
                MySQLPreparedStatement row = queue.poll();
                row.setConnection(connection);
                row.executeStatements();
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void addItem(String playerName, String itemName, double price, boolean disableBuy, boolean disableSell) {
        queue.add(new ItemRowStatement(itemName, price, !disableBuy, !disableSell));
    }

    @Override
    public void removeItem(String playerName, String itemName) {
        queue.add(new ItemDeleteStatement(itemName));
    }

    public static int getItemID(String name) throws SQLException {
        try (Connection connection = MySQLHandle.getConnection()) {
            String sql = "SELECT `id` FROM `market-items` WHERE `name` = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name.toUpperCase());
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        return results.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public static String getItemName(int id) throws SQLException {
        try (Connection connection = MySQLHandle.getConnection()) {
            String sql = "SELECT `name` FROM `market-items` WHERE `id` = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        return results.getString(1);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ItemPricePair getItem(String name) {
        try (Connection connection  = MySQLHandle.getConnection()) {
            String sql = "SELECT " + columns + " FROM `market-items` WHERE `name` = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name.toUpperCase());
                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        return new ItemPricePair(
                                results.getString(1),
                                results.getDouble(2),
                                !results.getBoolean(3),
                                !results.getBoolean(4)
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ItemPricePair> getItemList() {
        List<ItemPricePair> items = new ArrayList<>();
        try (Connection connection = MySQLHandle.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT " + columns + " FROM `market-items`")) {
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        items.add(new ItemPricePair(
                                results.getString(1),
                                results.getDouble(2),
                                !results.getBoolean(3),
                                !results.getBoolean(4)
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public List<ItemPricePair> getItemList(String filter, boolean showHidden) {

        if (filter == null || filter.isEmpty()) {
            return getItemList();
        }

        List<ItemPricePair> items = new ArrayList<>();
        try (Connection connection = MySQLHandle.getConnection()) {
            String sql = "SELECT " + columns + " FROM `market-items` WHERE `name` LIKE ?";
            if (!showHidden) {
                sql += " AND (`buyable` = true OR `sellable` = true)";
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, filter.toUpperCase() + "%");
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        items.add(new ItemPricePair(
                                results.getString(1),
                                results.getDouble(2),
                                !results.getBoolean(3),
                                !results.getBoolean(4)
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}
