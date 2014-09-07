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

package com.skelril.aurora.util.player;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

public class PlayerState extends GenericWealthStore implements Serializable {

    private double health = 20;
    private int hunger = 20;
    private float saturation = 5;
    private float exhaustion = 0;
    private int level = 0;
    private float experience = 0;
    private transient Location location = null;

    public PlayerState(String ownerName, ItemStack[] inventoryContents, ItemStack[] armorContents, int level,
                       float experience) {

        super(ownerName, inventoryContents, armorContents);
        this.level = level;
        this.experience = experience;
    }

    public PlayerState(String ownerName, ItemStack[] inventoryContents, ItemStack[] armorContents, double health,
                       int hunger, float saturation, float exhaustion, int level, float experience) {

        super(ownerName, inventoryContents, armorContents);
        this.health = health;
        this.hunger = hunger;
        this.saturation = saturation;
        this.exhaustion = exhaustion;
        this.level = level;
        this.experience = experience;
    }

    public PlayerState(String ownerName, ItemStack[] inventoryContents, ItemStack[] armorContents, double health,
                       int hunger, float saturation, float exhaustion, Location location) {

        super(ownerName, inventoryContents, armorContents);
        this.health = health;
        this.hunger = hunger;
        this.saturation = saturation;
        this.exhaustion = exhaustion;
        this.location = location == null ? null : location.clone();
    }

    public PlayerState(String ownerName, ItemStack[] inventoryContents, ItemStack[] armorContents, double health,
                       int hunger, float saturation, float exhaustion, int level, float experience, Location location) {

        super(ownerName, inventoryContents, armorContents);
        this.health = health;
        this.hunger = hunger;
        this.saturation = saturation;
        this.exhaustion = exhaustion;
        this.level = level;
        this.experience = experience;
        this.location = location == null ? null : location.clone();
    }


    public double getHealth() {

        return health;
    }

    public void setHealth(double health) {

        this.health = health;
    }

    public int getHunger() {

        return hunger;
    }

    public void setHunger(int hunger) {

        this.hunger = hunger;
    }

    public float getSaturation() {

        return saturation;
    }

    public void setSaturation(float saturation) {

        this.saturation = saturation;
    }

    public float getExhaustion() {

        return exhaustion;
    }

    public void setExhaustion(float exhaustion) {

        this.exhaustion = exhaustion;
    }

    public int getLevel() {

        return level;
    }

    public void setLevel(int level) {

        this.level = level;
    }

    public float getExperience() {

        return experience;
    }

    public void setExperience(float experience) {

        this.experience = experience;
    }

    public Location getLocation() {

        return location;
    }

    public void setLocation(Location location) {

        this.location = location == null ? null : location.clone();
    }

}
