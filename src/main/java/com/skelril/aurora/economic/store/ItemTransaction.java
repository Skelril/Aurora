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

package com.skelril.aurora.economic.store;

public class ItemTransaction {

    private final String player;
    private final String item;
    private final int amount;

    public ItemTransaction(String player, String item, int amount) {
        this.player = player;
        this.item = item;
        this.amount = amount;
    }

    public String getPlayer() {
        return player;
    }

    public String getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }
}
