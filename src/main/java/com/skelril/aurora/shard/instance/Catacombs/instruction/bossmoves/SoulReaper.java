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
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail.getFrom;

public class SoulReaper implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;
    private final int baseActivation;

    public SoulReaper() {
        this(15);
    }

    public SoulReaper(int baseActivation) {
        this(null, baseActivation);
    }

    public SoulReaper(InstructionResult<DamageInstruction> next, int baseActivation) {
        this.next = next;
        this.baseActivation = baseActivation;
    }

    public boolean activate(EntityDetail detail) {
        return ChanceUtil.getChance(baseActivation - CatacombEntityDetail.getFrom(detail).getWave());
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        CatacombEntityDetail detail = getFrom(condition.getBoss().getDetail());
        Entity attacked = condition.getAttacked();
        if (attacked instanceof Player && activate(detail)) {
            CommandBook.server().getScheduler().runTaskLater(CommandBook.inst(), () -> {
                double stolen = ((Player) attacked).getHealth() - 1;
                ((Player) attacked).setHealth(1);
                EntityUtil.heal(condition.getBoss().getEntity(), stolen);
                ChatUtil.sendWarning((Player) attacked, "The necromancer reaps your soul.");
            }, 1);
        }
        return next;
    }
}
