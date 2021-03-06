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
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class DropInstruction implements UnbindInstruction {

    private final InstructionResult<UnbindInstruction> next;

    public DropInstruction() {
        this(null);
    }

    public DropInstruction(InstructionResult<UnbindInstruction> next) {
        this.next = next;
    }

    public abstract List<ItemStack> getDrops(EntityDetail detail);

    public void dropItems(Location target, List<ItemStack> items) {
        for (ItemStack itemStack : items) {
            target.getWorld().dropItem(target, itemStack);
        }
    }

    @Override
    public InstructionResult<UnbindInstruction> process(UnbindCondition condition) {
        Boss boss = condition.getBoss();
        dropItems(boss.getEntity().getLocation(), getDrops(boss.getDetail()));
        return next;
    }
}
