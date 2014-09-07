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

package com.skelril.aurora.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Comparator;

public class EntityDistanceComparator implements Comparator<Entity> {

    final Location targetLoc;

    public EntityDistanceComparator(Location targetLoc) {

        this.targetLoc = targetLoc;
    }

    @Override
    public int compare(Entity o1, Entity o2) {

        return (int) (o1.getLocation().distanceSquared(targetLoc) - o2.getLocation().distanceSquared(targetLoc));
    }
}
