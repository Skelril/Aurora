/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.combat.bosses.instruction;

import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamageCondition;
import com.skelril.OpenBoss.instruction.DamageInstruction;
import com.skelril.aurora.combat.bosses.detail.WBossDetail;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.event.entity.EntityDamageEvent;

public class WDamageModifier implements DamageInstruction {

    private final InstructionResult<DamageInstruction> next;

    public WDamageModifier() {
        this(null);
    }

    public WDamageModifier(InstructionResult<DamageInstruction> next) {
        this.next = next;
    }

    @Override
    public InstructionResult<DamageInstruction> process(DamageCondition condition) {
        EntityDamageEvent event = condition.getEvent();
        if (event == null) return null;
        double origDmg = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
        int level = getWLevel(condition.getBoss());
        int addDmg = ChanceUtil.getRandom(ChanceUtil.getRandom(level)) - 1;
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, origDmg + addDmg);
        return next;
    }

    private int getWLevel(Boss boss) {
        EntityDetail detail = boss.getDetail();
        if (detail instanceof WBossDetail) {
            return ((WBossDetail) detail).getLevel();
        }
        return 1;
    }
}
