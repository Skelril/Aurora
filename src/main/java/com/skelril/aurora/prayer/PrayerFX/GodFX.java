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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Author: Turtle9598
 */
public class GodFX extends AbstractTriggeredEffect {

    private static final AbstractEffect[] subFX = new AbstractEffect[]{
            new ThrownFireballFX(), new InfiniteHungerFX(),
            new InvisibilityFX()
    };
    private static PotionEffect[] effects = new PotionEffect[]{
            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 600, 10),
            new PotionEffect(PotionEffectType.REGENERATION, 20 * 600, 10),
            new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 600, 10),
            new PotionEffect(PotionEffectType.WATER_BREATHING, 20 * 600, 10),
            new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 600, 10)
    };
    private static PotionEffectType[] removableEffects = new PotionEffectType[]{
            PotionEffectType.CONFUSION, PotionEffectType.BLINDNESS, PotionEffectType.WEAKNESS,
            PotionEffectType.POISON, PotionEffectType.SLOW
    };

    public GodFX() {

        super(PlayerInteractEvent.class, subFX, effects);
    }

    @Override
    public PrayerType getType() {

        return PrayerType.GOD;
    }

    @Override
    public void clean(Player player) {

        super.clean(player);

        for (PotionEffectType removableEffect : removableEffects) {
            player.removePotionEffect(removableEffect);
        }
    }

    @Override
    public void trigger(Player player) {

        for (AbstractEffect aSubFX : subFX) {
            if (aSubFX instanceof AbstractTriggeredEffect) {
                ((AbstractTriggeredEffect) aSubFX).trigger(player);
            }
        }
    }
}
