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
import org.bukkit.entity.Entity;

public abstract class ExplosiveUnbind implements UnbindInstruction {

    private final InstructionResult<UnbindInstruction> next;

    private final boolean blockBreak;
    private final boolean fire;

    protected ExplosiveUnbind(boolean blockBreak, boolean fire) {
        this(null, blockBreak, fire);
    }

    protected ExplosiveUnbind(InstructionResult<UnbindInstruction> next, boolean blockBreak, boolean fire) {
        this.next = next;
        this.blockBreak = blockBreak;
        this.fire = fire;
    }

    public abstract float getExplosionStrength(EntityDetail t);

    @Override
    public InstructionResult<UnbindInstruction> process(UnbindCondition condition) {
        Boss boss = condition.getBoss();
        Entity bossEnt = boss.getEntity();
        Location target = bossEnt.getLocation();

        target.getWorld().createExplosion(
                target.getX(), target.getY(), target.getZ(),
                getExplosionStrength(condition.getBoss().getDetail()),
                fire,
                blockBreak
        );
        return next;
    }
}
