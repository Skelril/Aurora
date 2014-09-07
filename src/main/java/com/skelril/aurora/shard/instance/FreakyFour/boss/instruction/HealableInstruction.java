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
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;

public class HealableInstruction implements DamagedInstruction {

    private static Set<EntityDamageEvent.DamageCause> healable = new HashSet<>();

    static {
        healable.add(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION);
        healable.add(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);
        healable.add(EntityDamageEvent.DamageCause.WITHER);
    }

    private final InstructionResult<DamagedInstruction> next;

    public HealableInstruction() {
        this(null);
    }

    public HealableInstruction(InstructionResult<DamagedInstruction> next) {
        this.next = next;
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        EntityDamageEvent event = condition.getEvent();
        if (healable.contains(event.getCause())) {
            event.setCancelled(true);
            EntityUtil.heal(condition.getBoss().getEntity(), event.getDamage());
        }
        return next;
    }
}
