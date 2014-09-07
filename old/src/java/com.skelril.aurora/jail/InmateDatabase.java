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

package com.skelril.aurora.jail;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface InmateDatabase extends Iterable<Inmate> {

    /**
     * Load the ban database.
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
     * Unloads the database
     *
     * @return whether the operation was fully successful
     */
    public boolean unload();

    /**
     * Checks if a player's name is jailed.
     *
     * @param ID The ID to check
     * @return Whether name is jailed
     */
    public boolean isInmate(UUID ID);

    /**
     * Returns a Inmate with the given ID
     *
     * @param ID The ID of the jailed player
     * @return The applicable Inmate
     */
    public Inmate getInmate(UUID ID);

    /**
     * Jails a player
     *
     * @param player
     * @param prison
     * @param source
     * @param reason
     * @param end
     * @param mute
     */
    public void jail(Player player, String prison, CommandSender source, String reason, long end, boolean mute);

    /**
     * Jails a player by ID
     *
     * @param ID
     * @param prison
     * @param source
     * @param reason
     * @param end
     * @param mute
     */
    public void jail(UUID ID, String prison, CommandSender source, String reason, long end, boolean mute);

    /**
     * Unjails a player
     *
     * @param player
     * @param source
     * @param reason
     */
    public boolean unjail(Player player, CommandSender source, String reason);

    /**
     * Unjails a player by ID
     *
     * @param ID
     * @param source
     * @param reason
     */
    public boolean unjail(UUID ID, CommandSender source, String reason);

    /**
     * Returns a list of inmates
     *
     * @return A list of inmates
     */
    public List<Inmate> getInmatesList();
}