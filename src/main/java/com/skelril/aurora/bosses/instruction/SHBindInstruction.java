/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.bosses.instruction;

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.BindCondition;
import com.skelril.OpenBoss.instruction.BindInstruction;
import org.bukkit.entity.LivingEntity;

public class SHBindInstruction extends NamedBindInstruction {

    private final double health;

    public SHBindInstruction(String name, double health) {
        this(null, name, health);
    }

    public SHBindInstruction(BindInstruction next, String name, double health) {
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
