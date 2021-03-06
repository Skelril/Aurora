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

package com.skelril.aurora.shard.instance.FreakyFour.boss.instruction;

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourConfig;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class BackTeleportInstruction implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;

    private FreakyFourConfig config;

    public BackTeleportInstruction(FreakyFourConfig config) {
        this(null, config);
    }

    public BackTeleportInstruction(InstructionResult<DamagedInstruction> next, FreakyFourConfig config) {
        this.next = next;
        this.config = config;
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        EntityDamageEvent event = condition.getEvent();

        Entity entity = event.getEntity();
        Entity damager = null;
        Projectile projectile = null;
        if (event instanceof EntityDamageByEntityEvent) {
            damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Projectile) {
                projectile = (Projectile) damager;
                ProjectileSource source = projectile.getShooter();
                if (source instanceof Entity) {
                    damager = (Entity) source;
                }
            }
        }
        boolean backTeleport = projectile == null && ChanceUtil.getChance(config.backTeleport);
        if ((backTeleport || projectile != null) && damager != null) {
            double distSQ = 2;
            double maxDist = 1;
            if (entity instanceof Skeleton) {
                distSQ = entity.getLocation().distanceSquared(damager.getLocation());
                maxDist = config.snipeeTeleportDist;
            }
            if (backTeleport || distSQ > Math.pow(maxDist, 2)) {
                final Entity finalDamager = damager;
                server().getScheduler().runTaskLater(inst(), () -> {
                    entity.teleport(finalDamager);
                    throwBack(entity);
                }, 1);
            }
        }
        return next;
    }

    private void throwBack(Entity entity) {
        Vector vel = entity.getLocation().getDirection();
        vel.multiply(-ChanceUtil.getRangedRandom(1.2, 1.5));
        vel.setY(Math.min(.8, Math.max(.175, vel.getY())));
        entity.setVelocity(vel);
    }
}
