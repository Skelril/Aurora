/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.FreakyFour.boss;

import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.aurora.combat.bosses.instruction.HealthPrint;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourBoss;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance;
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.entity.LivingEntity;

import static com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance.getInst;

public class CharlotteMinionManager extends BossManager {

    public CharlotteMinionManager() {
        handleDamage();
        handleDamaged();
    }

    private void handleDamage() {
        DamageProcessor damageProcessor = getDamageProcessor();
        damageProcessor.addInstruction(condition -> {
            FreakyFourInstance inst = getInst(condition.getBoss().getDetail());
            if (inst == null) return null;
            LivingEntity boss = inst.getBoss(FreakyFourBoss.CHARLOTTE);
            EntityUtil.heal(boss, condition.getEvent().getDamage());
            return null;
        });
    }

    private void handleDamaged() {
        DamagedProcessor damagedProcessor = getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
