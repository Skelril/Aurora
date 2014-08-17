/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.specialattack.attacks.melee.master;

import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.MeleeSpecial;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UltimateStrength extends EntityAttack implements MeleeSpecial {

    public UltimateStrength(LivingEntity owner, LivingEntity target) {
        super(owner, target);
    }

    @Override
    public void activate() {
        owner.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 30, 2));
        owner.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 30, 2));
        inform("You gain a new sense of true power.");
    }
}
