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

package com.skelril.aurora.homes;

import org.bukkit.entity.Player;

/**
 * Author: Turtle9598
 */
public interface HomeDatabase {

    /**
     * Load the home database.
     *
     * @return whether the operation was fully successful
     */
    public boolean load();

    /**
     * Save the database.
     *
     * @return whether the operation was fully successful
     */
    public boolean save();

    /**
     * Checks if a player has a house
     *
     * @param name The name to check
     * @return Whether the player has a house
     */
    public boolean houseExist(String name);

    /**
     * Jails a player
     *
     * @param player
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public void saveHouse(Player player, String world, int x, int y, int z);

    /**
     * Unjails a player by name
     *
     * @param player
     */
    public boolean deleteHouse(String player);

    /**
     * Returns the home with the given name
     *
     * @param name The name given to the ban.
     * @return The applicable player
     */
    public Home getHouse(String name);
}
