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

package com.skelril.aurora.items.specialattack.attacks.melee.unleashed;

import com.skelril.aurora.combat.PvPComponent;
import com.skelril.aurora.events.anticheat.RapidHitEvent;
import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.MeleeSpecial;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.DamageUtil;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class DoomBlade extends EntityAttack implements MeleeSpecial {

    public DoomBlade(LivingEntity owner, LivingEntity target) {
        super(owner, target);
    }

    @Override
    public void activate() {

        inform("Your weapon releases a huge burst of energy.");

        if (owner instanceof Player) {
            server.getPluginManager().callEvent(new RapidHitEvent((Player) owner));
        }

        double dmgTotal = 0;
        List<Entity> entityList = target.getNearbyEntities(6, 4, 6);
        entityList.add(target);
        for (Entity e : entityList) {
            if (e.isValid() && e instanceof LivingEntity) {
                if (e.equals(owner)) continue;
                double maxHit = ChanceUtil.getRangedRandom(150, 350);
                if (e instanceof Player) {
                    if (owner instanceof Player && !PvPComponent.allowsPvP((Player) owner, (Player) e)) {
                        continue;
                    }
                    maxHit = (1.0 / 3.0) * maxHit;
                }
                DamageUtil.damage(owner, (LivingEntity) e, maxHit);
                for (int i = 0; i < 20; i++) e.getWorld().playEffect(e.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
                dmgTotal += maxHit;
            }
        }
        inform("Your sword dishes out an incredible " + (int) Math.ceil(dmgTotal) + " damage!");
    }
}
