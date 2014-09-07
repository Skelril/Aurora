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
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class BlipDefense implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;

    public BlipDefense() {
        this(null);
    }

    public BlipDefense(InstructionResult<DamagedInstruction> next) {
        this.next = next;
    }

    public double getMultiplier() {
        return 4;
    }

    public double getYFloor() {
        return .175;
    }

    public double getYCiel() {
        return .8;
    }

    public boolean activate(EntityDetail detail) {
        return ChanceUtil.getChance(5);
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        Boss boss = condition.getBoss();
        if (activate(boss.getDetail())) {
            Entity bossEnt = boss.getEntity();
            Vector vel = bossEnt.getLocation().getDirection();
            vel.multiply(getMultiplier());
            vel.setY(Math.min(getYCiel(), Math.max(getYFloor(), vel.getY())));
            bossEnt.setVelocity(vel);
        }
        return next;
    }
}
