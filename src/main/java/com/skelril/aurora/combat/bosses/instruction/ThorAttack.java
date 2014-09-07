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

package com.skelril.aurora.combat.bosses.instruction;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class ThorAttack implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;

    public ThorAttack() {
        this(null);
    }

    public ThorAttack(InstructionResult<DamageInstruction> next) {
        this.next = next;
    }

    public boolean activate(EntityDetail detail) {
        return true;
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        Boss boss = condition.getBoss();
        if (activate(boss.getDetail())) {
            Entity bossEnt = boss.getEntity();
            Entity toHit = condition.getAttacked();
            toHit.setVelocity(bossEnt.getLocation().getDirection().multiply(2));

            server().getScheduler().runTaskLater(inst(), () -> {
                Location targetLocation = toHit.getLocation();
                server().getScheduler().runTaskLater(inst(), () -> {
                    targetLocation.getWorld().strikeLightning(targetLocation);
                }, 15);
            }, 30);
        }
        return next;
    }
}
