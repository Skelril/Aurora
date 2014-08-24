/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs.instruction;

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.UnbindCondition;
import com.skelril.OpenBoss.instruction.UnbindInstruction;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.skelril.aurora.shard.instance.Catacombs.CatacombsInstance.getInst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class CheckedSpawnWave implements UnbindInstruction {

    private final InstructionResult<UnbindInstruction> next;

    public CheckedSpawnWave() {
        this(null);
    }

    public CheckedSpawnWave(InstructionResult<UnbindInstruction> next) {
        this.next = next;
    }

    @Override
    public InstructionResult<UnbindInstruction> process(UnbindCondition condition) {
        server().getScheduler().runTaskLater(inst(), () -> {
            getInst(condition.getBoss().getDetail()).checkedSpawnWave();
        }, 1);
        return next;
    }
}
