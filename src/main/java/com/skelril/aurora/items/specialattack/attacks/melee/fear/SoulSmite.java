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

package com.skelril.aurora.items.specialattack.attacks.melee.fear;

import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.MeleeSpecial;
import org.bukkit.entity.LivingEntity;

public class SoulSmite extends EntityAttack implements MeleeSpecial {

    public SoulSmite(LivingEntity owner, LivingEntity target) {
        super(owner, target);
    }

    @Override
    public void activate() {

        final double targetHP = target.getHealth() / target.getMaxHealth();

        target.setHealth((targetHP / 2) * target.getMaxHealth());
        server.getScheduler().runTaskLater(inst, () -> {
            if (target.isValid()) {
                double newTargetHP = target.getHealth() / target.getMaxHealth();
                if (newTargetHP < targetHP) {
                    target.setHealth(target.getMaxHealth() * targetHP);
                }
            }
            inform("Your sword releases its grasp on its victim.");
        }, 20 * (int) Math.min(20, target.getMaxHealth() / 5 + 1));
        inform("Your sword steals its victims health for a short time.");
    }
}
