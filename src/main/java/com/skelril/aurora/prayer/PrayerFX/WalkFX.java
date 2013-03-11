package com.skelril.aurora.prayer.PrayerFX;

import com.skelril.aurora.prayer.PrayerType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Author: Turtle9598
 */
public class WalkFX extends AbstractPrayer {

    private static final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 20 * 600, 2);

    public WalkFX() {

        super(null, effect);
    }

    @Override
    public PrayerType getType() {

        return PrayerType.WALK;
    }
}