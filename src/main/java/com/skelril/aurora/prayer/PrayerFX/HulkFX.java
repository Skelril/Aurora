/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.prayer.PrayerFX;

import com.skelril.aurora.prayer.PrayerType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Author: Turtle9598
 */
public class HulkFX extends AbstractEffect {

    private static final AbstractEffect[] subFX = new AbstractEffect[]{
            new InfiniteHungerFX()
    };

    public HulkFX() {
        this(4);
    }

    public HulkFX(int tier) {
        super(
                subFX,
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 30, tier),
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 30, tier)
        );
    }

    @Override
    public PrayerType getType() {

        return PrayerType.HULK;
    }

    @Override
    public void clean(Player player) {

        super.clean(player);
    }
}