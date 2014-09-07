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
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class ThrownFireballFX extends AbstractTriggeredEffect {

    private long nextTime = -1;

    public ThrownFireballFX() {

        super(PlayerInteractEvent.class);
    }

    @Override
    public void trigger(Player player) {

        if (nextTime != -1 && System.currentTimeMillis() < nextTime) return;

        Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(2))
                .toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
        player.getWorld().spawn(loc, Fireball.class);

        nextTime = System.currentTimeMillis() + 750;
    }

    @Override
    public PrayerType getType() {

        return PrayerType.FIREBALL;
    }
}
