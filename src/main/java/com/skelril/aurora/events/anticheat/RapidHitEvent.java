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

package com.skelril.aurora.events.anticheat;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class RapidHitEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private int damage;

    public RapidHitEvent(Player player) {

        super(player);
        this.damage = -1;
    }

    public RapidHitEvent(Player player, int damage) {

        super(player);
        this.damage = damage;
    }

    public void setDamage(int damage) {

        Validate.isTrue(damage >= -1, "The damage must be greater than or equal to negative one");
        this.damage = damage;
    }

    /**
     * @return the damage or -1 if not applicable
     */
    public int getDamage() {

        return damage;
    }

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}