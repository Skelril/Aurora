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

package com.skelril.aurora.events.guild;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.List;

public class NinjaSmokeBombEvent extends NinjaEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private int explosionPower;
    private int delay;
    private List<Entity> entities;
    private Location originTeleportLoc;
    private Location teleportLoc;
    private Location targetLoc;

    public NinjaSmokeBombEvent(Player who, int explosionPower, int delay,
                               List<Entity> entities, Location teleportLoc, Location targetLoc) {
        super(who);
        this.explosionPower = explosionPower;
        this.delay = delay;
        this.entities = entities;
        this.originTeleportLoc = teleportLoc;
        this.targetLoc = targetLoc;
    }

    public int getExplosionPower() {
        return explosionPower;
    }

    public void setExplosionPower(int explosionPower) {
        this.explosionPower = explosionPower;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Location getOriginalTeleportLoc() {
        return originTeleportLoc;
    }

    public Location getTeleportLoc() {
        return teleportLoc;
    }

    public void setTeleportLoc(Location teleportLoc) {
        this.teleportLoc = teleportLoc;
    }

    public Location getTargetLoc() {
        return targetLoc.clone();
    }

    public void setTargetLoc(Location targetLoc) {
        this.targetLoc = targetLoc.clone();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
