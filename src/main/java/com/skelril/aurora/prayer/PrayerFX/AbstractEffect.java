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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEffect {

    private final Set<PotionEffect> effects = new HashSet<>();
    private final AbstractEffect[] subFX;

    public AbstractEffect() {

        this.subFX = null;
    }

    public AbstractEffect(AbstractEffect[] subFX) {

        this.subFX = subFX;
    }

    public AbstractEffect(AbstractEffect[] subFX, PotionEffect... effects) {

        this.subFX = subFX;
        Collections.addAll(this.effects, effects);
    }

    public abstract PrayerType getType();

    public Set<PotionEffect> getPotionEffects() {

        return effects;
    }

    public void add(Player player) {

        if (subFX != null) {
            for (AbstractEffect aSubFX : subFX) {
                aSubFX.add(player);
            }
        }
        player.addPotionEffects(effects);
    }

    public void clean(Player player) {

        if (subFX != null) {
            for (AbstractEffect aSubFX : subFX) {
                aSubFX.clean(player);
            }
        }
        for (PotionEffect effect : effects) {

            player.removePotionEffect(effect.getType());
        }
    }

    public void kill(Player player) {

        // Do nothing unless implemented
    }
}
