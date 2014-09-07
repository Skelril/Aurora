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

package com.skelril.aurora.shard.instance.Catacombs.instruction;

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail.getFrom;
import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BASE;

public class WaveDamageModifier implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;

    public WaveDamageModifier() {
        this(null);
    }

    public WaveDamageModifier(InstructionResult<DamageInstruction> next) {
        this.next = next;
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        CatacombEntityDetail detail = getFrom(condition.getBoss().getDetail());
        EntityDamageByEntityEvent event = condition.getEvent();
        event.setDamage(BASE, ChanceUtil.getRandom(ChanceUtil.getRandom(detail.getWave() * .2)) + event.getDamage(BASE));
        return next;
    }
}
