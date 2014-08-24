/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs.instruction;

import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.combat.bosses.instruction.CalculatedHealthInstruction;
import com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail;

public class CatacombsHealthInstruction extends CalculatedHealthInstruction {

    private final int baseHP;

    public CatacombsHealthInstruction(int baseHP) {
        this.baseHP = baseHP;
    }

    @Override
    public double getHealth(EntityDetail detail) {
        return CatacombEntityDetail.getFrom(detail).getWave() * baseHP;
    }
}
