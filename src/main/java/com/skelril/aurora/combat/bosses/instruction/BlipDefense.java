/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.combat.bosses.instruction;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class BlipDefense implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;

    public BlipDefense() {
        this(null);
    }

    public BlipDefense(InstructionResult<DamagedInstruction> next) {
        this.next = next;
    }

    public double getMultiplier() {
        return 4;
    }

    public double getYFloor() {
        return .175;
    }

    public double getYCiel() {
        return .8;
    }

    public boolean activate(EntityDetail detail) {
        return ChanceUtil.getChance(5);
    }

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        Boss boss = condition.getBoss();
        if (activate(boss.getDetail())) {
            Entity bossEnt = boss.getEntity();
            Vector vel = bossEnt.getLocation().getDirection();
            vel.multiply(getMultiplier());
            vel.setY(Math.min(getYCiel(), Math.max(getYFloor(), vel.getY())));
            bossEnt.setVelocity(vel);
        }
        return next;
    }
}
