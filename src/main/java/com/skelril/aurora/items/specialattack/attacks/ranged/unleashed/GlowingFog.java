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

package com.skelril.aurora.items.specialattack.attacks.ranged.unleashed;

import com.skelril.aurora.combat.PvPComponent;
import com.skelril.aurora.events.anticheat.RapidHitEvent;
import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.ranged.RangedSpecial;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.DamageUtil;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GlowingFog extends EntityAttack implements RangedSpecial {

    public GlowingFog(LivingEntity owner, LivingEntity target) {
        super(owner, target);
    }

    @Override
    public void activate() {
        final Location targeted = target.getLocation();

        IntegratedRunnable glowingFog = new IntegratedRunnable() {
            @Override
            public boolean run(int times) {

                if (owner instanceof Player) {
                    server.getPluginManager().callEvent(new RapidHitEvent((Player) owner));
                }

                EnvironmentUtil.generateRadialEffect(targeted, Effect.MOBSPAWNER_FLAMES);

                for (Entity aEntity : targeted.getWorld().getEntitiesByClasses(LivingEntity.class)) {
                    if (!aEntity.isValid() || aEntity.equals(owner)
                            || aEntity.getLocation().distanceSquared(targeted) > 16) continue;
                    if (aEntity instanceof LivingEntity) {
                        if (aEntity instanceof Player) {
                            if (owner instanceof Player && !PvPComponent.allowsPvP((Player) owner, (Player) aEntity))
                                continue;
                        }
                        DamageUtil.damage(owner, (LivingEntity) aEntity, 5);
                    }
                }
                return true;
            }

            @Override
            public void end() {

            }
        };

        TimedRunnable runnable = new TimedRunnable(glowingFog, (ChanceUtil.getRandom(15) * 3) + 7);
        runnable.setTask(server.getScheduler().runTaskTimer(inst, runnable, 0, 10));

        inform("Your bow unleashes a powerful glowing fog.");
    }
}
