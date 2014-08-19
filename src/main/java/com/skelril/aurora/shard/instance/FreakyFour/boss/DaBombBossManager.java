/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.FreakyFour.boss;

import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.combat.bosses.instruction.HealthPrint;
import com.skelril.aurora.combat.bosses.instruction.SHBindInstruction;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourConfig;
import com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance;
import com.skelril.aurora.shard.instance.FreakyFour.boss.instruction.BackTeleportInstruction;
import com.skelril.aurora.shard.instance.FreakyFour.boss.instruction.HealableInstruction;

import static com.skelril.aurora.shard.instance.FreakyFour.FreakyFourInstance.getInst;

public class DaBombBossManager extends BossManager {

    private FreakyFourConfig config;

    public DaBombBossManager(FreakyFourConfig config) {
        this.config = config;
        handleBinds();
        handleUnbinds();
        handleDamage();
        handleDamaged();
    }

    private void handleBinds() {
        BindProcessor bindProcessor = getBindProcessor();
        bindProcessor.addInstruction(new SHBindInstruction("Da Bomb", config.daBombHP));
    }

    private void handleUnbinds() {
        UnbindProcessor unbindProcessor = getUnbindProcessor();
        unbindProcessor.addInstruction(condition -> {
            FreakyFourInstance inst = getInst(condition.getBoss().getDetail());
            if (inst == null) return null;
            inst.bossDied(inst.getCurrentboss());
            return null;
        });
    }

    private void handleDamage() {

    }

    private void handleDamaged() {
        DamagedProcessor damagedProcessor = getDamagedProcessor();
        damagedProcessor.addInstruction(new BackTeleportInstruction(config));
        damagedProcessor.addInstruction(new HealableInstruction());
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
