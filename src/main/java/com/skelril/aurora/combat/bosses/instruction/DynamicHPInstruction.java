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
import com.skelril.OpenBoss.condition.BindCondition;
import com.skelril.OpenBoss.instruction.BindInstruction;
import org.bukkit.entity.LivingEntity;

public abstract class DynamicHPInstruction extends NamedBindInstruction {

    public DynamicHPInstruction(String name) {
        this(null, name);
    }

    public DynamicHPInstruction(InstructionResult<BindInstruction> next, String name) {
        super(next, name);
    }

    public abstract double getHealth(EntityDetail detail);

    @Override
    public InstructionResult<BindInstruction> process(BindCondition condition) {
        Boss boss = condition.getBoss();
        LivingEntity anEntity = condition.getBoss().getEntity();
        double health = getHealth(boss.getDetail());
        anEntity.setMaxHealth(health);
        anEntity.setHealth(health);
        return super.process(condition);
    }
}
