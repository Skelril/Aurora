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

package com.skelril.aurora.items.specialattack.attacks.hybrid.unleashed;

import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.MeleeSpecial;
import com.skelril.aurora.items.specialattack.attacks.ranged.RangedSpecial;
import org.bukkit.entity.LivingEntity;

public class LifeLeech extends EntityAttack implements MeleeSpecial, RangedSpecial {

    public LifeLeech(LivingEntity owner, LivingEntity target) {
        super(owner, target);
    }

    @Override
    public void activate() {

        final double ownerMax = owner.getMaxHealth();

        final double ownerHP = owner.getHealth() / ownerMax;
        final double targetHP = target.getHealth() / target.getMaxHealth();

        if (ownerHP > targetHP) {
            owner.setHealth(Math.min(ownerMax, ownerMax * (ownerHP + .1)));
            inform("Your weapon heals you.");
        } else {
            target.setHealth(target.getMaxHealth() * ownerHP);
            owner.setHealth(Math.min(ownerMax, ownerMax * targetHP * 1.1));
            inform("You leech the health of your foe.");
        }
    }
}
