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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ArrowUtil {

    public static Arrow shootArrow(LivingEntity entity, LivingEntity targetEntity, float power, float accuracy) {

        Location shooterLoc = entity.getLocation().add(0, entity.getEyeHeight(), 0);
        Location targetLoc = targetEntity.getLocation().add(0, targetEntity.getEyeHeight(), 0);

        Arrow arrow = shootArrow(shooterLoc, targetLoc, power, accuracy);
        if (arrow == null) return null;
        arrow.setShooter(entity);
        return arrow;
    }

    public static Arrow shootArrow(Location startingLocation, Location endingLocation, float power, float accuracy) {


        double locY = startingLocation.getY() - 0.10000000149011612D;
        double d0 = endingLocation.getX() - startingLocation.getX();
        double d1 = endingLocation.getY() - 1.699999988079071D - locY;
        double d2 = endingLocation.getZ() - startingLocation.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        if (d3 >= 1.0E-7D) {
            float f2 = (float) (Math.atan2(d2, d0) * 180.0D / 3.1415927410125732D) - 90.0F;
            float f3 = (float) (-(Math.atan2(d1, d3) * 180.0D / 3.1415927410125732D));
            double d4 = d0 / d3;
            double d5 = d2 / d3;

            Location startLoc = new Location(startingLocation.getWorld(), startingLocation.getX() + d4,
                    locY + 1, startingLocation.getZ() + d5, f2, f3);
            float f4 = (float) d3 * 0.2F;

            return startingLocation.getWorld().spawnArrow(startLoc,
                    new Vector(d0, d1 + (double) f4, d2), power, accuracy);
        }
        return null;
    }

    public static Vector getVelocity(Location startingLocation, Location endingLocation) {

        double locY = startingLocation.getY() - 0.10000000149011612D;
        double d0 = endingLocation.getX() - startingLocation.getX();
        double d1 = endingLocation.getY() - 1.699999988079071D - locY;
        double d2 = endingLocation.getZ() - startingLocation.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        if (d3 >= 1.0E-7D) {
            float f4 = (float) d3 * 0.2F;
            return new Vector(d0, d1 + (double) f4, d2);
        }
        return null;
    }
}
