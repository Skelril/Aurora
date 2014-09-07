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

package com.skelril.aurora.economic.lottery.mysql;

import com.skelril.aurora.data.MySQLPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TicketAdditionStatement implements MySQLPreparedStatement {

    private Connection con;
    private final int player;
    private final int amount;

    public TicketAdditionStatement(int player, int amount) {
        this.player = player;
        this.amount = amount;
    }

    @Override
    public void setConnection(Connection con) {
        this.con = con;
    }

    @Override
    public void executeStatements() throws SQLException {
        String sql = "INSERT INTO `lottery-tickets` (player, tickets) VALUES (?, ?)"
                + "ON DUPLICATE KEY UPDATE tickets=values(tickets) + tickets";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, player);
            statement.setInt(2, amount);
            statement.execute();
        }
    }
}
