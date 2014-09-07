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

package com.skelril.aurora.city.engine.minigame;

import com.skelril.aurora.util.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;

public class PlayerGameState extends PlayerState implements Serializable {

    private int teamNumber = 0;

    private String worldName;
    private double x, y, z;
    private float yaw, pitch;

    public PlayerGameState(PlayerState state, int teamNumber) {
        super(state.getOwnerName(), state.getInventoryContents(), state.getArmourContents(), state.getHealth(),
                state.getHunger(), state.getSaturation(), state.getExhaustion(), state.getLevel(),
                state.getExperience());
        this.teamNumber = teamNumber;
        setLocation(state.getLocation());
    }

    public int getTeamNumber() {

        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {

        this.teamNumber = teamNumber;
    }

    @Override
    public void setLocation(Location location) {

        super.setLocation(location);

        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public Location getLocation() {

        Location k = super.getLocation();

        if (k == null) {
            try {
                k = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
            } catch (Exception ex) {
                k = Bukkit.getWorlds().get(0).getSpawnLocation();
            }

            super.setLocation(k);
        }

        return k;
    }
}
