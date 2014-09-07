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

package com.skelril.aurora.items.specialattack.attacks.melee.master;

import com.skelril.aurora.events.anticheat.RapidHitEvent;
import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.MeleeSpecial;
import com.skelril.aurora.util.DamageUtil;
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.Effect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class HealingLight extends EntityAttack implements MeleeSpecial {

    public HealingLight(LivingEntity owner, LivingEntity target) {
        super(owner, target);
    }

    @Override
    public void activate() {
        if (owner instanceof Player) {
            server.getPluginManager().callEvent(new RapidHitEvent((Player) owner));
        }

        EntityUtil.heal(owner, 5);
        for (int i = 0; i < 4; i++) {
            target.getWorld().playEffect(target.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        }

        DamageUtil.damage(owner, target, 20);
        inform("Your weapon glows dimly.");
    }
}
