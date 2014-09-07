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

package com.skelril.aurora.events.entity;

import org.bukkit.entity.Projectile;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

/**
 * Author: Turtle9598
 */
public class ProjectileTickEvent extends EntityEvent {

    private static final HandlerList handlers = new HandlerList();
    private final float force;

    public ProjectileTickEvent(final Projectile projectile, final float force) {

        super(projectile);
        this.force = force;
    }

    @Override
    public Projectile getEntity() {

        return (Projectile) super.getEntity();
    }

    public boolean hasLaunchForce() {

        return force != -1;
    }

    public float getLaunchForce() {

        return force;
    }

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
