/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.specialattack.attacks.melee.master;

import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.MeleeSpecial;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Blind extends EntityAttack implements MeleeSpecial {

    public Blind(LivingEntity owner, LivingEntity target) {
        super(owner, target);
    }

    @Override
    public void activate() {
        if (target instanceof Player) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 4, 0), true);
            inform("Your weapon blinds your victim.");
        } else {
            new HealingLight(owner, target).activate();
        }
    }
}
