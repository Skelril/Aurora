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

package com.skelril.aurora.prayer.PrayerFX;

import com.skelril.aurora.prayer.PrayerType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AbsorptionFX extends AbstractEffect {

    private static final PotionEffect effect = new PotionEffect(PotionEffectType.ABSORPTION, 20 * 600, 4);

    public AbsorptionFX() {

        super(null, effect);
    }

    @Override
    public void clean(Player player) {

        // Don't clean this effect, it messes it up
    }

    @Override
    public void kill(Player player) {

        player.removePotionEffect(PotionEffectType.ABSORPTION);
    }

    @Override
    public PrayerType getType() {

        return PrayerType.ABSORPTION;
    }
}