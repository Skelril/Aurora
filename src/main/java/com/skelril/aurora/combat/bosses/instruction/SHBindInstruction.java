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

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.BindCondition;
import com.skelril.OpenBoss.instruction.BindInstruction;
import org.bukkit.entity.LivingEntity;

public class SHBindInstruction extends NamedBindInstruction {

    private final double health;

    public SHBindInstruction(String name, double health) {
        this(null, name, health);
    }

    public SHBindInstruction(InstructionResult<BindInstruction> next, String name, double health) {
        super(next, name);
        this.health = health;
    }

    @Override
    public InstructionResult<BindInstruction> process(BindCondition condition) {
        LivingEntity anEntity = condition.getBoss().getEntity();
        anEntity.setMaxHealth(health);
        anEntity.setHealth(health);
        return super.process(condition);
    }
}
