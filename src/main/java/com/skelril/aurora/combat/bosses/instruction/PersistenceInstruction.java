/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.combat.bosses.instruction;

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.BindCondition;
import com.skelril.OpenBoss.instruction.BindInstruction;

public class PersistenceInstruction implements BindInstruction {

    private final InstructionResult<BindInstruction> next;

    public PersistenceInstruction() {
        this(null);
    }

    public PersistenceInstruction(InstructionResult<BindInstruction> next) {
        this.next = next;
    }

    @Override
    public InstructionResult<BindInstruction> process(BindCondition condition) {
        condition.getBoss().getEntity().setRemoveWhenFarAway(false);
        return next;
    }
}
