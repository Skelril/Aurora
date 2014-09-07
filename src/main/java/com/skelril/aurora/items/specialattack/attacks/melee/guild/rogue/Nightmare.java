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

package com.skelril.aurora.items.specialattack.attacks.melee.guild.rogue;

import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.MeleeSpecial;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Nightmare extends EntityAttack implements MeleeSpecial {

    private Random r;

    public Nightmare(LivingEntity owner, LivingEntity target) {
        super(owner, target);
        r = new Random(System.currentTimeMillis());
    }

    @Override
    public void activate() {

        inform("You unleash a nightmare upon the plane.");

        final Set<Location> locations = new HashSet<>();

        Location origin = target.getLocation().add(0, 5, 0);

        for (int i = 0; i < 100; i++) {

            double angle = r.nextDouble() * Math.PI * 2;
            double radius = r.nextDouble() * 12;

            Location pt = origin.clone();
            pt.setX(origin.getX() + radius * Math.cos(angle));
            pt.setZ(origin.getZ() + radius * Math.sin(angle));

            locations.add(pt);
        }

        IntegratedRunnable hellFire = new IntegratedRunnable() {
            @Override
            public boolean run(int times) {
                locations.stream().filter(location -> ChanceUtil.getChance(3)).forEach(location -> {
                    Snowball snowball = location.getWorld().spawn(location, Snowball.class);
                    snowball.setMetadata("rogue-snowball", new FixedMetadataValue(inst, true));
                    snowball.setMetadata("nightmare", new FixedMetadataValue(inst, true));
                    snowball.setShooter(owner);
                });
                return true;
            }

            @Override
            public void end() {
                inform("Your nightmare fades away...");
            }
        };

        TimedRunnable runnable = new TimedRunnable(hellFire, 40);
        runnable.setTask(server.getScheduler().runTaskTimer(inst, runnable, 50, 10));
    }
}
