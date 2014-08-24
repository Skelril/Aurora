/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs.instruction.bossmoves;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail;
import com.skelril.aurora.shard.instance.Catacombs.CatacombsInstance;
import com.skelril.aurora.util.ChanceUtil;

import static com.skelril.aurora.shard.instance.Catacombs.CatacombsInstance.getInst;

public class UndeadMinionRetaliation implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;
    private final int baseActivation;

    public UndeadMinionRetaliation() {
        this(100);
    }

    public UndeadMinionRetaliation(int baseActivation) {
        this(null, baseActivation);
    }

    public UndeadMinionRetaliation(InstructionResult<DamagedInstruction> next, int baseActivation) {
        this.next = next;
        this.baseActivation = baseActivation;
    }

    public boolean activate(EntityDetail detail) {
        return ChanceUtil.getChance(baseActivation - CatacombEntityDetail.getFrom(detail).getWave());
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        Boss boss = condition.getBoss();
        CatacombsInstance instance = getInst(boss.getDetail());
        if (activate(boss.getDetail())) {
            instance.spawnWaveMob(boss.getEntity().getLocation());
        }
        return next;
    }
}
