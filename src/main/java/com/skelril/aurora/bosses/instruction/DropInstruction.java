/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.bosses.instruction;

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
