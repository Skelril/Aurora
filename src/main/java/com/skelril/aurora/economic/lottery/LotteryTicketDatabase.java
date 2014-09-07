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

package com.skelril.aurora.economic.lottery;

import com.skelril.aurora.util.player.GenericWealthStore;

import java.util.List;

/**
 * Author: Turtle9598
 */
public interface LotteryTicketDatabase {

    public boolean load();

    public boolean save();

    public void addTickets(String playerName, int count);

    public int getTickets(String playerName);

    public void clearTickets();

    public int getTicketCount();

    public List<GenericWealthStore> getTickets();
}
