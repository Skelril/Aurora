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
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.Random;

public class DeathUtil {

    private static final Random random = new Random();

    private static final PotionType[] thrownTypes = new PotionType[]{
            PotionType.INSTANT_DAMAGE, PotionType.INSTANT_DAMAGE,
            PotionType.POISON, PotionType.WEAKNESS
    };

    public static void throwSlashPotion(Location location) {

        ThrownPotion potionEntity = location.getWorld().spawn(location, ThrownPotion.class);
        PotionType type = CollectionUtil.getElement(thrownTypes);
        Potion potion = new Potion(type);
        potion.setLevel(type.getMaxLevel());
        potion.setSplash(true);
        potionEntity.setItem(potion.toItemStack(1));
        potionEntity.setVelocity(new Vector(
                random.nextDouble() * .5 - .25,
                random.nextDouble() * .4 + .1,
                random.nextDouble() * .5 - .25
        ));
    }
}
