/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.Catacombs.instruction.bossmoves;

import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail.getFrom;

public class SoulReaper implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;
    private final int baseActivation;

    public SoulReaper() {
        this(100);
    }

    public SoulReaper(int baseActivation) {
        this(null, baseActivation);
    }

    public SoulReaper(InstructionResult<DamageInstruction> next, int baseActivation) {
        this.next = next;
        this.baseActivation = baseActivation;
    }

    public boolean activate(EntityDetail detail) {
        return ChanceUtil.getChance(baseActivation - CatacombEntityDetail.getFrom(detail).getWave());
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        CatacombEntityDetail detail = getFrom(condition.getBoss().getDetail());
        Entity attacked = condition.getAttacked();
        if (attacked instanceof Player && activate(detail)) {
            double stolen = ((Player) attacked).getHealth() - 1;
            ((Player) attacked).setHealth(1);
            EntityUtil.heal(condition.getBoss().getEntity(), stolen);
            ChatUtil.sendWarning((Player) attacked, "The necromancer reaps your soul.");
        }
        return next;
    }
}
