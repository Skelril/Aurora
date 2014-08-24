/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.combat.bosses.instruction;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.BindCondition;
import com.skelril.OpenBoss.instruction.BindInstruction;
import org.bukkit.entity.LivingEntity;

public abstract class CalculatedHealthInstruction implements BindInstruction {

    private final InstructionResult<BindInstruction> next;

    public CalculatedHealthInstruction() {
        this(null);
    }

    public CalculatedHealthInstruction(InstructionResult<BindInstruction> next) {
        this.next = next;
    }

    public abstract double getHealth(EntityDetail detail);

    @Override
    public InstructionResult<BindInstruction> process(BindCondition condition) {
        Boss boss = condition.getBoss();
        LivingEntity anEntity = condition.getBoss().getEntity();
        double health = getHealth(boss.getDetail());
        anEntity.setMaxHealth(health);
        anEntity.setHealth(health);
        return next;
    }
}
