/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs.instruction;

import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail.getFrom;
import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BASE;

public class WaveDamageModifier implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;

    public WaveDamageModifier() {
        this(null);
    }

    public WaveDamageModifier(InstructionResult<DamageInstruction> next) {
        this.next = next;
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        CatacombEntityDetail detail = getFrom(condition.getBoss().getDetail());
        EntityDamageByEntityEvent event = condition.getEvent();
        event.setDamage(BASE, ChanceUtil.getRandom(ChanceUtil.getRandom(detail.getWave() * .2)) + event.getDamage(BASE));
        return next;
    }
}
