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

package com.skelril.aurora.shard.instance.Catacombs.instruction.bossmoves;

import com.sk89q.commandbook.CommandBook;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;

import java.util.Set;

import static com.sk89q.commandbook.util.entity.EntityUtil.sendProjectilesFromEntity;

public class ExplosiveArrowBarrage implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;

    public ExplosiveArrowBarrage() {
        this(null);
    }

    public ExplosiveArrowBarrage(InstructionResult<DamagedInstruction> next) {
        this.next = next;
    }

    public boolean activate(EntityDetail detail) {
        return true;
    }

    public float explosionStrength(EntityDetail detail) {
        return 4F;
    }

    public boolean allowFire(EntityDetail detail) {
        return false;
    }

    public boolean allowBlockBreak(EntityDetail detail) {
        return false;
    }

    public long getDelay(EntityDetail detail) {
        return 20 * 7;
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        Boss boss = condition.getBoss();
        EntityDetail bossDetail = boss.getDetail();
        if (activate(bossDetail)) {
            Set<Arrow> arrows = sendProjectilesFromEntity(boss.getEntity(), 20, 1.6F, Arrow.class);
            CommandBook.server().getScheduler().runTaskLater(CommandBook.inst(), () -> {
                for (Arrow arrow : arrows) {
                    Location target = arrow.getLocation();
                    target.getWorld().createExplosion(
                            target.getX(), target.getY(), target.getZ(),
                            explosionStrength(bossDetail),
                            allowFire(bossDetail),
                            allowBlockBreak(bossDetail)
                    );
                }
            }, getDelay(bossDetail));
        }
        return next;
    }
}
