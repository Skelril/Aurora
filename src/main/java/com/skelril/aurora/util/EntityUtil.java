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

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class EntityUtil {
    public static boolean nameMatches(Entity entity, String name) {
        if (entity == null || !(entity instanceof LivingEntity)) return false;
        String customName = ((LivingEntity) entity).getCustomName();
        return customName != null && customName.equals(name);
    }

    public static void heal(Entity entity, double amt) {
        if (entity == null || !entity.isValid() || !(entity instanceof LivingEntity)) return;
        double cur = ((LivingEntity) entity).getHealth();
        double max = ((LivingEntity) entity).getMaxHealth();

        ((LivingEntity) entity).setHealth(Math.min(cur + amt, max));
    }

    public static void forceDamage(Entity entity, double amt) {
        if (entity == null || !entity.isValid() || !(entity instanceof LivingEntity)) return;
        double cur = ((LivingEntity) entity).getHealth();

        ((LivingEntity) entity).setHealth(Math.max(cur - amt, 0));
    }
}
