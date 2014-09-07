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
import com.skelril.aurora.util.EnvironmentUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Author: Turtle9598
 */
public class SmokeFX extends AbstractEffect {

    @Override
    public PrayerType getType() {

        return PrayerType.SMOKE;
    }

    @Override
    public void add(Player player) {

        Location[] smoke = new Location[2];
        smoke[0] = player.getLocation();
        smoke[1] = player.getEyeLocation();
        EnvironmentUtil.generateRadialEffect(smoke, Effect.SMOKE);
    }

    @Override
    public void clean(Player player) {

        // Nothing to do here
    }
}
