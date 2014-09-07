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
import com.skelril.OpenBoss.condition.UnbindCondition;
import com.skelril.OpenBoss.instruction.UnbindInstruction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class ExplosiveUnbind implements UnbindInstruction {

    private final InstructionResult<UnbindInstruction> next;

    private final boolean blockBreak;
    private final boolean fire;

    protected ExplosiveUnbind(boolean blockBreak, boolean fire) {
        this(null, blockBreak, fire);
    }

    protected ExplosiveUnbind(InstructionResult<UnbindInstruction> next, boolean blockBreak, boolean fire) {
        this.next = next;
        this.blockBreak = blockBreak;
        this.fire = fire;
    }

    public abstract float getExplosionStrength(EntityDetail t);

    @Override
    public InstructionResult<UnbindInstruction> process(UnbindCondition condition) {
        Boss boss = condition.getBoss();
        Entity bossEnt = boss.getEntity();
        Location target = bossEnt.getLocation();

        target.getWorld().createExplosion(
                target.getX(), target.getY(), target.getZ(),
                getExplosionStrength(condition.getBoss().getDetail()),
                fire,
                blockBreak
        );
        return next;
    }
}
